// ============================================
// KOPIRAJTE CEO OVAJ FAJL U BROWSER KONZOLU!
// ============================================
// 
// UPUTSTVO:
// 1. Otvorite aplikaciju u browseru
// 2. Pritisnite F12 → Console tab
// 3. Kopirajte CEO OVAJ FAJL (Ctrl+A, Ctrl+C)
// 4. Zalepite u konzolu (Ctrl+V)
// 5. Pritisnite Enter
// 6. Zamenite VIDEO_ID (linija ispod) sa stvarnim ID-jem videa
// 7. Pokrenite: testConcurrentViews()
// ============================================

// ⚠️ VAŽNO: Zamenite ovaj broj sa stvarnim ID-jem vašeg videa!
const VIDEO_ID = 9; // <-- OVDE ZAMENITE BROJ!
const API_BASE_URL = 'http://localhost:8080';
const CONCURRENT_USERS = 50;
const VIEWS_PER_USER = 2;

async function getVideoViews(videoId) {
    const response = await fetch(`${API_BASE_URL}/videos/${videoId}`);
    if (!response.ok) throw new Error(`Greška ${response.status}`);
    const video = await response.json();
    return video.views || 0;
}

async function incrementViews(videoId) {
    const response = await fetch(`${API_BASE_URL}/videos/view/${videoId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error(`Greška ${response.status}`);
    const video = await response.json();
    return video.views;
}

async function testConcurrentViews() {
    console.log('🚀 Počinje test istovremenih pregleda...');
    console.log(`📹 Video ID: ${VIDEO_ID}`);
    console.log(`👥 Korisnika: ${CONCURRENT_USERS}, Pregleda po korisniku: ${VIEWS_PER_USER}`);
    console.log(`📊 Očekivano: ${CONCURRENT_USERS * VIEWS_PER_USER} pregleda`);
    console.log('');

    let initialViews = await getVideoViews(VIDEO_ID);
    console.log(`📈 Početni pregledi: ${initialViews}`);

    const startTime = Date.now();
    const promises = [];
    let successCount = 0;

    for (let i = 0; i < CONCURRENT_USERS; i++) {
        for (let j = 0; j < VIEWS_PER_USER; j++) {
            promises.push(
                incrementViews(VIDEO_ID)
                    .then(() => { successCount++; })
                    .catch(err => console.error(`❌ Greška:`, err))
            );
        }
    }

    console.log(`⏳ Čekanje ${promises.length} pregleda...`);
    await Promise.all(promises);

    const duration = ((Date.now() - startTime) / 1000).toFixed(2);
    let finalViews = await getVideoViews(VIDEO_ID);
    const expectedViews = initialViews + (CONCURRENT_USERS * VIEWS_PER_USER);

    console.log('');
    console.log('📊 === REZULTATI ===');
    console.log(`📈 Početni: ${initialViews}`);
    console.log(`📈 Finalni: ${finalViews}`);
    console.log(`📈 Očekivano: ${expectedViews}`);
    console.log(`✅ Uspešno: ${successCount}`);
    console.log(`⏱️  Vreme: ${duration}s`);
    console.log('');

    if (finalViews === expectedViews) {
        console.log('🎉 TEST PROŠAO! Broj pregleda je tačan!');
    } else {
        const diff = Math.abs(finalViews - expectedViews);
        console.log(`⚠️  Razlika: ${diff} pregleda`);
        if (diff <= 2) {
            console.log('✅ Razlika je minimalna - test je uspešan!');
        }
    }
}

testConcurrentViews();
