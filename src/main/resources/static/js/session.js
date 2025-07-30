// Session management utilities
//sesson.js
class SessionManager {
    static startSession(sessionId, mode) {
        localStorage.setItem('currentSessionId', sessionId);
        localStorage.setItem('sessionMode', mode);
        localStorage.setItem('lastActivity', Date.now());
    }

    static getActiveSession() {
        return {
            id: localStorage.getItem('currentSessionId'),
            mode: localStorage.getItem('sessionMode')
        };
    }

    static endSession() {
        localStorage.removeItem('currentSessionId');
        localStorage.removeItem('sessionMode');
        localStorage.removeItem('lastActivity');
    }

    static sessionExists() {
        return localStorage.getItem('currentSessionId') !== null;
    }

    static checkSessionExpiration() {
        const lastActivity = localStorage.getItem('lastActivity');
        if (lastActivity && Date.now() - lastActivity > 30 * 60 * 1000) { // 30 minutes
            this.endSession();
        }
        localStorage.setItem('lastActivity', Date.now());
    }
}

// Initialize session checking
setInterval(() => SessionManager.checkSessionExpiration(), 5 * 60 * 1000); // Check every 5 minutes