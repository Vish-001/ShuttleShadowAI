class SessionRunner {
    constructor(mode, sessionId) {
        this.mode = mode.toUpperCase();
        this.sessionId = sessionId;
        this.zones = ["TOP_LEFT", "TOP_RIGHT", "CENTER", "BOTTOM_LEFT", "BOTTOM_RIGHT"];
        this.currentZone = null;
        this.startTime = null;
        this.data = {};
        this.roundsCompleted = 0;
        this.maxRounds = 10;
        this.centerClicked = false;
        this.bestReactionTime = Infinity;
        this.weakestZone = "BOTTOM_LEFT"; // Hardcoded for now; can be dynamic from backend

        // Initialize UI
        document.getElementById('sessionModeDisplay').textContent = this.mode === 'MEDIUM' ? 'Standard Mode' : 'Improve Mode';
        document.getElementById('sessionIdDisplay').textContent = this.sessionId;

        // Setup error handling
        window.onerror = (message) => this.showError(message);

        try {
            this.initializeSession();
        } catch (e) {
            this.showError("Failed to initialize session");
            console.error("Session initialization error:", e);
        }
    }

    initializeSession() {
        this.zones.forEach(z => {
            this.data[z] = { total: 0, hits: 0, reactionTimes: [] };
        });

        this.zones.forEach(zone => {
            const element = document.getElementById(zone);
            if (element) {
                element.addEventListener('click', () => this.handleZoneClick(zone));
            }
        });

        this.nextRound();
    }

    nextRound() {
        if (this.roundsCompleted >= this.maxRounds) {
            this.endSession();
            return;
        }

        this.centerClicked = false;
        this.activateCenter();
    }

    activateCenter() {
        this.resetAllTargets();
        this.currentZone = "CENTER";
        const center = document.getElementById("CENTER");
        if (center) {
            center.classList.add("active");
            this.startTime = Date.now();
        }
    }

    activateCorner() {
        this.resetAllTargets();
        const corners = ["TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"];
        this.currentZone = this.mode === 'IMPROVE' && Math.random() < 0.66 ? this.weakestZone : corners[Math.floor(Math.random() * 4)];
        const corner = document.getElementById(this.currentZone);
        if (corner) {
            corner.classList.add("active");
            this.startTime = Date.now();
        }
    }

    handleZoneClick(zoneId) {
        if (!this.currentZone || zoneId !== this.currentZone) {
            this.showWrongClickFeedback(zoneId);
            return;
        }

        const reactionTime = Date.now() - this.startTime;
        this.recordHit(zoneId, reactionTime);

        if (zoneId === "CENTER") {
            this.centerClicked = true;
            this.activateCorner();
        } else if (this.centerClicked) {
            this.roundsCompleted++;
            this.updateRoundCounter();
            setTimeout(() => this.nextRound(), 500);
        }
    }

    recordHit(zoneId, reactionTime) {
        if (!this.data[zoneId]) return;

        this.data[zoneId].hits++;
        this.data[zoneId].total += reactionTime;
        this.data[zoneId].reactionTimes.push(reactionTime);

        if (reactionTime < this.bestReactionTime) {
            this.bestReactionTime = reactionTime;
        }

        this.updateStats(reactionTime);
    }

    updateStats(reactionTime) {
        const currentTimeElement = document.getElementById("currentTime");
        const averageTimeElement = document.getElementById("averageTime");
        const bestTimeElement = document.getElementById("bestTime");

        if (currentTimeElement) {
            currentTimeElement.textContent = `${reactionTime}ms`;
        }

        const totalHits = Object.values(this.data).reduce((sum, zone) => sum + zone.hits, 0);
        const totalTime = Object.values(this.data).reduce((sum, zone) => sum + zone.total, 0);
        const avg = totalHits > 0 ? Math.round(totalTime / totalHits) : 0;

        if (averageTimeElement) {
            averageTimeElement.textContent = `${avg}ms`;
        }
        if (bestTimeElement) {
            bestTimeElement.textContent = `${this.bestReactionTime === Infinity ? 0 : this.bestReactionTime}ms`;
        }
    }

    updateRoundCounter() {
        const roundCountElement = document.getElementById("roundCount");
        if (roundCountElement) {
            roundCountElement.textContent = `${this.roundsCompleted}/${this.maxRounds}`;
        }
    }

    resetAllTargets() {
        this.zones.forEach(z => {
            const element = document.getElementById(z);
            if (element) {
                element.classList.remove("active");
            }
        });
    }

    showWrongClickFeedback(zoneId) {
        const element = document.getElementById(zoneId);
        if (element) {
            element.style.background = "radial-gradient(circle, #e74c3c, #c0392b)";
            setTimeout(() => {
                element.style.background = "";
                // Restore original background based on zone
                if (zoneId === "TOP_LEFT") element.style.background = "radial-gradient(circle, #f44336, #d32f2f)";
                if (zoneId === "TOP_RIGHT") element.style.background = "radial-gradient(circle, #4caf50, #2e7d32)";
                if (zoneId === "CENTER") element.style.background = "radial-gradient(circle, #ffeb3b, #ffc107)";
                if (zoneId === "BOTTOM_LEFT") element.style.background = "radial-gradient(circle, #2196f3, #1976d2)";
                if (zoneId === "BOTTOM_RIGHT") element.style.background = "radial-gradient(circle, #9c27b0, #7b1fa2)";
            }, 200);
        }
    }

    showError(message) {
        const errorDiv = document.getElementById('sessionError');
        const errorMessage = document.getElementById('errorMessage');

        if (errorDiv && errorMessage) {
            errorMessage.textContent = message;
            errorDiv.style.display = 'block';
        }
    }

    endSession() {
        const results = this.prepareResults();

        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'session-loading';
        loadingDiv.innerHTML = '<div class="session-loading-spinner"></div>';
        document.body.appendChild(loadingDiv);

        const requestData = {
            sessionId: this.sessionId,
            zonePerformances: results
        };

        fetch("/session/submit", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) throw new Error("Submission failed");
            return response.text();
        })
        .then(() => {
            window.location.href = "/dashboard?success=true";
        })
        .catch(error => {
            console.error("Submission error:", error);
            document.body.removeChild(loadingDiv);
            this.showError("Failed to save session results");
        });
    }

    prepareResults() {
        return this.zones
            .filter(zone => this.data[zone] && this.data[zone].hits > 0)
            .map(zone => ({
                zone: zone,
                averageReactionTime: this.data[zone].total / this.data[zone].hits,
                hits: this.data[zone].hits
            }));
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (window.sessionMode && window.sessionId) {
        try {
            new SessionRunner(window.sessionMode, window.sessionId);
        } catch (e) {
            console.error("Failed to start session runner:", e);
            const errorDiv = document.getElementById('sessionError');
            if (errorDiv) {
                errorDiv.style.display = 'block';
            }
        }
    } else {
        console.error("Missing session parameters");
        window.location.href = "/dashboard?error=session_params_missing";
    }
});