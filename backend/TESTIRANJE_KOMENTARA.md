# Uputstvo za testiranje komentara

## 1. Testiranje Rate Limiting-a (60 komentara po satu)

### Način 1: Pokretanje JUnit testova

```bash
cd backend
mvn test -Dtest=CommentRateLimitTest
```

Ili pokrenite sve testove:
```bash
mvn test
```

### Način 2: Ručno testiranje kroz API

1. **Prijavite se** kao korisnik (dobijte JWT token)
2. **Pronađite ID videa** na koji želite da komentarišete
3. **Pošaljite 60 komentara** koristeći POST zahtev:

```bash
# Primer sa curl (zamenite YOUR_TOKEN i VIDEO_ID)
for i in {1..60}; do
  curl -X POST http://localhost:8080/comments \
    -H "Authorization: Bearer YOUR_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"text\":\"Komentar $i\",\"videoId\":VIDEO_ID}"
  echo "Komentar $i poslat"
done
```

4. **Pokušajte da pošaljete 61. komentar** - trebalo bi da dobijete grešku:
   - Status: `429 Too Many Requests`
   - Poruka: `"Prekoračili ste limit od 60 komentara po satu. Molimo sačekajte."`

### Način 3: Testiranje kroz Frontend

1. Otvorite aplikaciju u browseru
2. Prijavite se
3. Otvorite Developer Tools (F12) → Console
4. Pokrenite JavaScript skriptu (pogledajte `test-comments.js`)

## 2. Testiranje Paginacije

### Način 1: Kroz Frontend

1. **Kreirajte veliki broj komentara** (npr. 25+ komentara) na jednom videu
2. **Idite na stranicu videa**
3. **Proverite da:**
   - Prikazuje se "Komentari (25)" ili koliko god ima
   - Ako ima više od 10 komentara (default page size), trebalo bi da vidi:
     - Kontrole "Prethodna" i "Sledeća"
     - Tekst "Strana 1 od X"
   - Klikom na "Sledeća" trebalo bi da se učitaju sledeći komentari
   - Komentari su sortirani od najnovijeg do najstarijeg

### Način 2: Direktno kroz API

```bash
# Uzmi prvu stranicu (10 komentara)
curl http://localhost:8080/comments/video/VIDEO_ID/paginated?page=0&size=10

# Uzmi drugu stranicu
curl http://localhost:8080/comments/video/VIDEO_ID/paginated?page=1&size=10

# Uzmi treću stranicu
curl http://localhost:8080/comments/video/VIDEO_ID/paginated?page=2&size=10
```

**Očekivani odgovor:**
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "hasNext": true,
  "hasPrevious": false
}
```

### Način 3: Pokretanje Load Test-a

```bash
cd backend
mvn test -Dtest=CommentLoadTest
```

Ovaj test automatski:
- Kreira 150 komentara
- Testira paginaciju
- Proverava sortiranje

## 3. Provera da li radi keširanje

1. **Pošaljite GET zahtev** za komentare:
```bash
curl http://localhost:8080/comments/video/VIDEO_ID/paginated?page=0&size=10
```

2. **Pošaljite isti zahtev ponovo** - trebalo bi da bude brži (keširan)

3. **Dodajte novi komentar** kroz POST

4. **Pošaljite GET zahtev ponovo** - keš bi trebalo da se osveži i novi komentar bi trebalo da se vidi

## 4. Provera hronološkog sortiranja

Komentari bi trebalo da budu sortirani od **najnovijeg do najstarijeg**:

1. Dodajte nekoliko komentara sa razmakom (npr. 1 sekund između)
2. Proverite da je najnoviji komentar na vrhu liste
3. Proverite da je najstariji komentar na dnu liste

## 5. Testiranje sa više korisnika

1. **Prijavite se kao korisnik A** i dodajte 30 komentara
2. **Prijavite se kao korisnik B** i dodajte 30 komentara
3. **Prijavite se ponovo kao korisnik A** - trebalo bi da može da doda još 30 komentara (ukupno 60)
4. **Pokušajte da dodate 31. komentar kao korisnik A** - trebalo bi da bude blokirano

## Troubleshooting

### Rate limiting ne radi?
- Proverite da li je korisnik autentifikovan (JWT token)
- Proverite logove backenda za greške
- Proverite da li su komentari kreirani u poslednjem satu

### Paginacija ne radi?
- Proverite da li ima dovoljno komentara (više od page size)
- Proverite browser konzolu za JavaScript greške
- Proverite Network tab u Developer Tools da vidite API odgovore

### Komentari nisu sortirani?
- Proverite da li `createdAt` polje postoji u bazi
- Proverite da li repository metoda koristi `OrderByCreatedAtDesc`
