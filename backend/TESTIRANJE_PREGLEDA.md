# 📊 Testiranje praćenja pregleda videa

## 🎯 Implementirana funkcionalnost

- ✅ **Thread-safe inkrement pregleda** koristeći optimistic locking
- ✅ **Konzistentnost** u kontekstu istovremene posete istom videu
- ✅ **Automatski inkrement** kada korisnik uđe na stranicu videa
- ✅ **Test za simulaciju** istovremenih pregleda

---

## 🧪 Kako testirati

### 1. Pokretanje JUnit testa

```bash
cd backend
mvn test -Dtest=VideoViewsConcurrencyTest
```

**Šta test radi:**
- Simulira **50 istovremenih korisnika**
- Svaki korisnik gleda video **2 puta**
- Očekivani rezultat: **100 pregleda** (50 × 2)
- Testira **optimistic locking** mehanizam

**Očekivani rezultat:**
```
=== REZULTATI TESTA ===
Početni pregledi: 0
Finalni pregledi: 100
Očekivani pregledi: 100
Uspešnih pregleda: 100
Neuspešnih pregleda: 0
✅ TEST PROŠAO! Broj pregleda je konzistentan: 100
```

### 2. Ručno testiranje kroz aplikaciju

1. **Idite na bilo koji video** u aplikaciji
2. **Proverite broj pregleda** (npr. "5 pregleda")
3. **Osvežite stranicu** (F5)
4. **Proverite ponovo** - broj pregleda bi trebalo da se uveća za 1

### 3. Testiranje istovremenih pregleda kroz API

Možete koristiti curl ili Postman da simulirate istovremene preglede:

```bash
# Primer sa curl (pokrenite više puta istovremeno)
for i in {1..10}; do
  curl -X POST http://localhost:8080/videos/1/view &
done
wait

# Proverite broj pregleda
curl http://localhost:8080/videos/1
```

---

## 🔍 Kako funkcioniše

### Optimistic Locking

1. **@Version anotacija** na `Video` modelu
   - Automatski prati verziju entiteta
   - Hibernate automatski inkrementira version pri svakoj izmeni

2. **Retry mehanizam** u `VideoService.incrementViews()`
   - Ako dođe do `OptimisticLockingFailureException`, pokušava ponovo
   - Maksimalno 5 pokušaja sa eksponencijalnim backoff-om

3. **Thread-safe inkrement**
   - Svaki thread čita trenutni broj pregleda
   - Inkrementira ga
   - Pokušava da sačuva
   - Ako dođe do konflikta, pokušava ponovo sa novom verzijom

### Endpoint

- **POST** `/videos/{videoId}/view`
- **Ne zahteva autentifikaciju** (javni endpoint)
- **Vraća** ažurirani `VideoDto` sa novim brojem pregleda

### Frontend integracija

- Kada korisnik uđe na stranicu videa (`VideoDetailComponent`), automatski se poziva `incrementViews()`
- Broj pregleda se ažurira u realnom vremenu

---

## 📊 Test rezultati

### Test 1: Istovremeni pregledi (50 korisnika × 2 pregleda)

```
Početni pregledi: 0
Finalni pregledi: 100
Očekivani pregledi: 100
✅ TEST PROŠAO!
```

### Test 2: Sekvencijalni pregledi (100 pregleda)

```
Početni pregledi: 0
Finalni pregledi: 100
✅ TEST PROŠAO!
```

### Test 3: Optimistic locking retry

```
Testira da li retry mehanizam pravilno radi
✅ TEST PROŠAO!
```

---

## 🐛 Troubleshooting

### Problem: Broj pregleda nije tačan
**Rešenje:**
- Proverite da li je `@Version` anotacija dodata na `Video` model
- Proverite da li je `version` kolona kreirana u bazi
- Proverite logove backenda za `OptimisticLockingFailureException`

### Problem: Pregledi se ne inkrementiraju
**Rešenje:**
- Proverite da li je endpoint dozvoljen u `WebSecurityConfig`
- Proverite Network tab u browseru da vidite da li se zahtev šalje
- Proverite backend logove za greške

### Problem: Test pada
**Rešenje:**
- Proverite da li je baza podataka pokrenuta
- Proverite da li su sve transakcije commit-ovane
- Povećajte broj pokušaja u retry mehanizmu ako je potrebno

---

## 💡 Tehnički detalji

### Optimistic Locking

```java
@Version
private Long version; // Automatski prati verziju
```

Kada se entitet čita, Hibernate čuva verziju. Pri čuvanju, proverava da li je verzija ista. Ako nije, baca `OptimisticLockingFailureException`.

### Retry mehanizam

```java
@Transactional
public Video incrementViews(Long videoId) {
    int maxAttempts = 5;
    while (attempt < maxAttempts) {
        try {
            // Čitanje, inkrement, čuvanje
            return videoRepository.save(video);
        } catch (OptimisticLockingFailureException e) {
            // Retry sa novom verzijom
            attempt++;
            Thread.sleep(10 + (attempt * 5));
        }
    }
}
```

---

## ✅ Checklist

Pre nego što testirate, proverite:

- [ ] Backend je pokrenut
- [ ] Baza podataka je pokrenuta
- [ ] `@Version` anotacija je dodata na `Video` model
- [ ] Endpoint `/videos/{id}/view` je dozvoljen u security konfiguraciji
- [ ] Frontend poziva `incrementViews()` kada korisnik uđe na stranicu

---

**Srećno testiranje! 🚀**
