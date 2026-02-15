// ============================================
// KOPIRAJTE CEO OVAJ FAJL U BROWSER KONZOLU!
// ============================================
// 
// UPUTSTVO:
// 1. Otvorite aplikaciju u browseru i prijavite se
// 2. Pritisnite F12 → Console tab
// 3. Kopirajte CEO OVAJ FAJL (Ctrl+A, Ctrl+C)
// 4. Zalepite u konzolu (Ctrl+V)
// 5. Pritisnite Enter
// 6. Zamenite VIDEO_ID (linija ispod) sa stvarnim ID-jem videa
// 7. Pokrenite: testRateLimiting()
// ============================================

// ⚠️ VAŽNO: Zamenite ovaj broj sa stvarnim ID-jem vašeg videa!
const VIDEO_ID = 1; // <-- OVDE ZAMENITE BROJ!
const API_BASE_URL = 'http://localhost:8080';
const COMMENTS_TO_SEND = 65;

function getToken() {
    return localStorage.getItem('jwt');
}

async function createComment(text, videoId) {
    const token = getToken();
    if (!token) {
        throw new Error('Niste prijavljeni! Molimo prijavite se prvo.');
    }

    const response = await fetch(`${API_BASE_URL}/comments`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            text: text,
            videoId: videoId
        })
    });

    const data = await response.json();
    
    if (!response.ok) {
        throw new Error(`Greška ${response.status}: ${data.message || JSON.stringify(data)}`);
    }

    return data;
}

async function testRateLimiting() {
    console.log('🚀 Počinje test rate limiting-a...');
    console.log(`📹 Video ID: ${VIDEO_ID}`);
    console.log(`📝 Planirano komentara: ${COMMENTS_TO_SEND}`);
    console.log('');

    let successCount = 0;
    let failedCount = 0;
    const startTime = Date.now();

    for (let i = 1; i <= COMMENTS_TO_SEND; i++) {
        try {
            await createComment(`Test komentar #${i} - ${new Date().toLocaleTimeString()}`, VIDEO_ID);
            successCount++;
            console.log(`✅ Komentar ${i}/${COMMENTS_TO_SEND} uspešno poslat`);
            await new Promise(resolve => setTimeout(resolve, 100));
        } catch (error) {
            failedCount++;
            console.error(`❌ Komentar ${i}/${COMMENTS_TO_SEND} neuspešan:`, error.message);
            if (error.message.includes('429') || error.message.includes('Prekoračili ste limit')) {
                console.log('');
                console.log('⛔ RATE LIMIT DOSTIGNUT!');
                console.log(`📊 Uspešno poslato: ${successCount} komentara`);
                console.log(`📊 Blokirano: ${failedCount} komentara`);
                break;
            }
        }
    }

    const endTime = Date.now();
    const duration = ((endTime - startTime) / 1000).toFixed(2);

    console.log('');
    console.log('📊 REZULTATI TESTA:');
    console.log(`✅ Uspešno poslato: ${successCount} komentara`);
    console.log(`❌ Neuspešno: ${failedCount} komentara`);
    console.log(`⏱️  Vreme izvršavanja: ${duration} sekundi`);
    
    if (successCount === 60 && failedCount > 0) {
        console.log('');
        console.log('🎉 TEST PROŠAO! Rate limiting radi ispravno - tačno 60 komentara je dozvoljeno.');
    } else if (successCount < 60) {
        console.log('');
        console.log('⚠️  Napomena: Možda već imate komentare iz prethodnog sata.');
    }
}

testRateLimiting();