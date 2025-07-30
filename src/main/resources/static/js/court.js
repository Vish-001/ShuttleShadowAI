//court.js
let zones = ["TOP_LEFT", "TOP_RIGHT", "CENTER", "BOTTOM_LEFT", "BOTTOM_RIGHT"];
let currentZone = null;
let startTime = null;
let data = {};
let attempts = 0;
let maxAttempts = 10;

function startPractice(sessionId, mode) {
    data = {};
    zones.forEach(z => {
        data[z] = { total: 0, hits: 0 };
    });
    nextFlash();
}

function nextFlash() {
    if (attempts >= maxAttempts) {
        document.getElementById("status").innerText = "Submitting data...";
        submitResults();
        return;
    }

    currentZone = zones[Math.floor(Math.random() * zones.length)];
    document.getElementById(currentZone).style.backgroundColor = "#f39c12";
    startTime = Date.now();

    setTimeout(() => {
        if (currentZone) {
            document.getElementById(currentZone).style.backgroundColor = "#ddd";
            currentZone = null;
            nextFlash();
        }
    }, 1500);
}

function zoneClicked(zoneId) {
    if (!currentZone || zoneId !== currentZone) return;

    const reactionTime = Date.now() - startTime;
    data[zoneId].total += reactionTime;
    data[zoneId].hits += 1;

    document.getElementById(zoneId).style.backgroundColor = "#2ecc71";
    setTimeout(() => {
        document.getElementById(zoneId).style.backgroundColor = "#ddd";
        attempts++;
        nextFlash();
    }, 200);
}

function submitResults() {
    const results = [];

    zones.forEach(zone => {
        if (data[zone].hits > 0) {
            results.push({
                zone: zone,
                averageReactionTime: data[zone].total / data[zone].hits,
                hits: data[zone].hits
            });
        }
    });

    fetch("/session/submit", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(results)
    })
    .then(response => response.text())
    .then(resp => {
        if (resp === "OK") {
            window.location.href = "/dashboard";
        } else {
            document.getElementById("status").innerText = "Error submitting data.";
        }
    });
}
