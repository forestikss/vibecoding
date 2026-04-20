package ru.etc1337.api.game;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@UtilityClass
public class Sound {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void playSound(final String location, double volume, float pitch) {
        executorService.execute(() -> {
            try {
                InputStream inputStream = Sound.class.getResourceAsStream("/assets/minecraft/dreamcore/sounds/" + location);
                if (inputStream == null) {
                    System.err.println("Audio not found: " + location);
                    return;
                }

                try (BufferedInputStream bufferedIn = new BufferedInputStream(inputStream);
                     AudioInputStream baseStream = AudioSystem.getAudioInputStream(bufferedIn)) {

                    AudioInputStream pitchedStream = resampleStream(baseStream, pitch);

                    Clip clip = AudioSystem.getClip();
                    clip.open(pitchedStream);

                    setVolume(clip, volume);
                    clip.start();
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        });
    }

    public void playSound(final String location) {
        playSound(location, 0.5, 1.0f);
    }

    private void setVolume(Clip clip, double volume) {
        if (volume < 0) volume = 0;
        if (volume > 1) volume = 1;

        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log10(volume <= 0 ? 0.0001 : volume) * 20);
        volumeControl.setValue(dB);
    }

    private AudioInputStream resampleStream(AudioInputStream originalStream, float pitch) throws IOException {
        AudioFormat originalFormat = originalStream.getFormat();

        byte[] audioBytes = originalStream.readAllBytes();

        float newSampleRate = originalFormat.getSampleRate() * pitch;

        AudioFormat newFormat = new AudioFormat(
                newSampleRate,
                originalFormat.getSampleSizeInBits(),
                originalFormat.getChannels(),
                true,
                originalFormat.isBigEndian()
        );

        return new AudioInputStream(
                new BufferedInputStream(new java.io.ByteArrayInputStream(audioBytes)),
                newFormat,
                audioBytes.length / newFormat.getFrameSize()
        );
    }


    @Getter
    @Setter
    public class AudioClipPlayController {
        private final AudioClip audioClip;
        private Supplier<Boolean> playIf;
        private boolean stopIsAPause;
        private boolean started;

        private AudioClipPlayController(AudioClip audioClip, Supplier<Boolean> playIf, boolean stopIsAPause) {
            this.audioClip = audioClip;
            this.playIf = playIf;
            this.stopIsAPause = stopIsAPause;
        }

        public static AudioClipPlayController build(AudioClip audioClip, Supplier<Boolean> playIf, boolean stopIsAPause) {
            return new AudioClipPlayController(audioClip, playIf, stopIsAPause);
        }

        public void updatePlayingStatus() {
            if (started && audioClip.clip == null && playIf.get()) {
                started = false;
            }
            if (!started && playIf.get()) {
                audioClip.startPlayingAudio();
                started = true;
            }
            if (stopIsAPause) {
                audioClip.setPause(!playIf.get());
                return;
            }
            if (audioClip.isPlaying() != playIf.get()) {
                if (playIf.get()) audioClip.startPlayingAudio();
                else audioClip.stopPlayingAudio();
            }
        }

        public boolean isSucessPlaying() {
            return this.audioClip.isPlaying();
        }
    }

    public class AudioClip {
        private final boolean loop;
        private boolean pause;
        private long currentPlayTime;
        @Getter
        private String soundName;
        private Clip clip;
        private float pitch = 1.0f;
        private float volume = 1.0f;

        private AudioClip(String soundName, boolean loop) {
            this.soundName = soundName;
            this.loop = loop;
        }

        public static AudioClip build(String soundName, boolean loop) {
            return new AudioClip(soundName, loop);
        }

        public boolean isPlaying() {
            return this.clip != null && this.clip.isOpen() && this.clip.isRunning();
        }

        public void changeAudioTrack(String soundName) {
            this.soundName = soundName;
            stopPlayingAudio();
            startPlayingAudio();
        }

        public void setLoop(boolean loop) {
            if (this.clip == null) return;
            this.clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
        }

        public void setPause(boolean pause) {
            if (this.pause != pause && clip != null && clip.isOpen()) {
                if (pause) {
                    currentPlayTime = clip.getMicrosecondPosition();
                    clip.stop();
                } else {
                    clip.setMicrosecondPosition(currentPlayTime);
                    clip.start();
                }
                this.pause = pause;
            }
        }

        public void setVolume(float volume) {
            this.volume = Math.max(0.0f, Math.min(1.0f, volume));
            if (this.clip == null || !clip.isOpen()) return;

            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            double dbValue = 20.0 * Math.log10(volume <= 0.0f ? 0.0001f : volume);
            control.setValue((float) dbValue);
        }

        public void setPitch(float pitch) {
            this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
        }

        public void startPlayingAudio() {
            this.stopPlayingAudio();
            try {
                this.clip = AudioSystem.getClip();
                String resourcePath = "/assets/minecraft/dreamcore/sounds/" + this.soundName;
                InputStream audioSrc = Sound.class.getResourceAsStream(resourcePath);
                if (audioSrc == null) {
                    System.err.println("Audio file not found: " + resourcePath);
                    return;
                }

                try (BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                     AudioInputStream baseStream = AudioSystem.getAudioInputStream(bufferedIn)) {

                    AudioInputStream pitchedStream = resampleStream(baseStream, this.pitch);
                    clip.open(pitchedStream);
                    setVolume(this.volume);
                    setLoop(this.loop);
                    clip.start();
                }
            } catch (Exception exception) {
                System.err.println("Error playing audio: " + exception.getMessage());
            }
        }

        public void stopPlayingAudio() {
            if (this.clip == null) return;
            if (this.clip.isRunning()) this.clip.stop();
            if (this.clip.isOpen()) this.clip.close();
            this.clip = null;
        }
    }
}
