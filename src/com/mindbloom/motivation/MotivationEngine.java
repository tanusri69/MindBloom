package com.mindbloom.motivation;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;


public class MotivationEngine {
    private MotivationProvider provider;
    private final Random random = new Random();

    
    public MotivationEngine() {
        this.provider = createDefaultProvider();
    }

    
    private MotivationProvider createDefaultProvider() {
        return (name) -> {
            String[] messages = {
                    "✨ Keep going, " + name + "! You're stronger than you think!",
                    "🌟 Every day is a fresh opportunity, " + name + ". Make it count!",
                    "💪 You've got this, " + name + "! Progress over perfection!",
                    "🎯 Small steps lead to big wins, " + name + ". Keep pushing!",
                    "🚀 Believe in yourself, " + name + ". You're capable of amazing things!",
                    "❤️ Be kind to yourself, " + name + ". You're doing great!",
                    "🌱 Growth is a journey, " + name + ". You're on the right path!",
                    "⭐ Your efforts matter, " + name + ". Don't give up!"
            };
            return messages[random.nextInt(messages.length)];
        };
    }

    
    public void setProvider(MotivationProvider provider) {
        if (provider != null) {
            this.provider = provider;
        }
    }

    
    public void showMotivation(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = "Friend";
        }
        System.out.println("\n" + provider.motivate(username) + "\n");
    }

    
    public void showMotivation(String username, String mood) {
        if (username == null || username.trim().isEmpty()) {
            username = "Friend";
        }
        String message = getMoodBasedMessage(username, mood);
        System.out.println("\n" + message + "\n");
    }

    private String getMoodBasedMessage(String name, String mood) {
        if (mood == null || mood.trim().isEmpty()) {
            return provider.motivate(name);
        }
        String key = mood.trim().toLowerCase();

        Map<String, String[]> moodMap = new HashMap<>();
        moodMap.put("happy", new String[]{
                "😄 Hey %s — keep that smile shining! Celebrate the little wins.",
                "🌞 %s, your joy is contagious — share it and multiply it!"
        });
        moodMap.put("sad", new String[]{
                "💛 %s, it's okay to feel sad. Be gentle with yourself today.",
                "🌧️ %s, tough days pass. Small comforts matter — take one step."
        });
        moodMap.put("anxious", new String[]{
                "🧘 %s, breathe. Focus on one small thing you can control.",
                "🌿 %s, this moment will pass — you're stronger than the worry."
        });
        moodMap.put("calm", new String[]{
                "🌊 %s, enjoy the calm — it's fertile ground for growth.",
                "🌱 %s, calm breeds clarity. Keep nurturing it."
        });
        moodMap.put("tired", new String[]{
                "😌 %s, rest is productive. Give yourself permission to recharge.",
                "🛌 %s, small breaks today will make tomorrow brighter."
        });
        moodMap.put("angry", new String[]{
                "🔥 %s, channel that energy constructively — one step at a time.",
                "⚖️ %s, pause and breathe before you act — you deserve clarity."
        });

        String[] candidates = moodMap.get(key);
        if (candidates == null) {
            
            for (Map.Entry<String, String[]> e : moodMap.entrySet()) {
                if (key.contains(e.getKey())) {
                    candidates = e.getValue();
                    break;
                }
            }
        }

        if (candidates == null) {
            return provider.motivate(name);
        }

        String chosen = candidates[random.nextInt(candidates.length)];
        return String.format(chosen, name);
    }
}
