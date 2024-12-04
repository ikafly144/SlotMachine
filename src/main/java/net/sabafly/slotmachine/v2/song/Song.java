package net.sabafly.slotmachine.v2.song;


import co.aikar.taskchain.TaskChain;
import net.sabafly.slotmachine.SlotMachine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.*;

public class Song {

    /**
     * A single arpeggio to symbolize success.
     */
    public static final Song CLICK_CHORD = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0, 1.2f)
    ));
    public static final Song START = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.059463f, 4, 1f), // 1.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.059463f, 4, 1f), // 3.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.059463f, 2, 1f), // 4.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.414214f, 2, 1f), // 5.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.587401f, 4, 1f), // 7.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.781797f, 4, 1f)  // 9.mcfunction
    ));
    public static final Song GRAPE = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.059463f, 2, 1f), // 0.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.890899f, 2, 1f), // 1.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.707107f, 2, 1f), // 2.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.059463f, 2, 1f), // 3.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.890899f, 2, 1f), // 4.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.707107f, 2, 1f), // 5.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.059463f, 2, 1f), // 6.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.890899f, 2, 1f), // 7.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.707107f, 2, 1f), // 8.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.059463f, 2, 1f), // 9.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.890899f, 2, 1f), // 10.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.707107f, 2, 1f)  // 11.mcfunction
    ));
    public static final Song REPLAY = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.414214f, 4, 1f), // 0.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.334840f, 2, 1f), // 2.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.189207f, 2, 1f), // 3.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BIT, 1.059463f, 2, 1f)  // 4.mcfunction
    ));
    public static final Song BIG_WIN = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.793701f, 2, 1f), // 0.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.793701f, 4, 1f), // 2.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 2, 1f), // 3.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.189207f, 2, 1f), // 4.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.334840f, 2, 1f), // 5.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.890899f, 2, 1f), // 6.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.890899f, 4, 1f), // 8.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.189207f, 2, 1f), // 9.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.334840f, 2, 1f), // 10.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.890899f, 2, 1f), // 11.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.943874f, 2, 1f), // 12.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.943874f, 4, 1f), // 14.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.334840f, 2, 1f), // 15.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.587401f, 2, 1f), // 16.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.887749f, 2, 1f), // 17.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 2, 1f), // 18.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 4, 1f), // 20.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 21.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.781797f, 2, 1f), // 22.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 2, 1f), // 23.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 24.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 4, 1f), // 26.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.334840f, 2, 1f), // 27.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.189207f, 2, 1f), // 28.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.334840f, 2, 1f), // 29.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 2, 1f), // 30.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 4, 1f), // 32.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.943874f, 2, 1f), // 33.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.890899f, 2, 1f), // 34.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 2, 1f), // 35.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.793701f, 2, 1f), // 36.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.793701f, 4, 1f), // 38.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.059463f, 2, 1f), // 39.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.189207f, 2, 1f), // 40.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.334840f, 2, 1f), // 41.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 42.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 43.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 44.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 45.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f), // 46.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.414214f, 2, 1f)  // 47.mcfunction
    ));
    public static final Song BIG_BGM = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.666649f, 1, 1f), // 0.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.666649f, 3, 1f), // 3.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.706290f, 1, 1f), // 4.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 1, 1f), // 5.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.058240f, 3, 1f), // 8.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.998845f, 3, 1f), // 11.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.889870f, 3, 1f), // 14.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.998845f, 3, 1f), // 17.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 3, 1f), // 20.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.058240f, 3, 1f), // 23.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.998845f, 3, 1f), // 26.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.889870f, 1, 1f), // 27.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 1, 1f), // 28.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.706290f, 3, 1f), // 31.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.666649f, 3, 1f), // 34.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.706290f, 3, 1f), // 37.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 3, 1f), // 40.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.666649f, 6, 1f), // 46.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.666649f, 3, 1f), // 49.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.706290f, 1, 1f), // 50.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 1, 1f), // 51.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.058240f, 3, 1f), // 54.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.998845f, 3, 1f), // 57.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.889870f, 3, 1f), // 60.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.998845f, 3, 1f), // 63.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 3, 1f), // 66.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.058240f, 3, 1f), // 69.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.998845f, 3, 1f), // 72.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.889870f, 1, 1f), // 73.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.792784f, 1, 1f), // 74.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.706290f, 3, 1f), // 77.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.666649f, 3, 1f), // 80.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.593917f, 3, 1f), // 83.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.529120f, 3, 1f) // 86.mcfunction
    ));
    public static final Song BIG_END = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.594604f, 2, 1f), // 0.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.667420f, 4, 1f), // 2.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.707107f, 4, 1f), // 4.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.594604f, 4, 1f), // 6.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.667420f, 4, 1f), // 8.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.707107f, 4, 1f), // 10.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.529732f, 4, 1f), // 12.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.529732f, 4, 1f), // 14.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.529732f, 2, 1f), // 15.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.707107f, 2, 1f), // 16.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.890899f, 4, 1f), // 18.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.793701f, 4, 1f), // 20.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.667420f, 4, 1f), // 22.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.707107f, 4, 1f), // 24.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.707107f, 4, 1f)  // 26.mcfunction
    ));
    public static final Song LOSE = new Song(List.of(
            new Note(Sound.BLOCK_NOTE_BLOCK_BASS, 0.707107f, 2, 1f), // 0.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BASS, 0.707107f, 2, 1f), // 2.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BASS, 0.707107f, 2, 1f), // 4.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BASS, 0.707107f, 2, 1f), // 6.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BASS, 0.707107f, 2, 1f), // 8.mcfunction
            new Note(Sound.BLOCK_NOTE_BLOCK_BASS, 0.707107f, 2, 1f)  // 10.mcfunction
    ));

    private final List<Note> notes = new LinkedList<>();

    /**
     * General constructor that takes some list of notes.
     *
     * @param notes the notes of the song
     */
    public Song(List<Note> notes) {
        this.notes.addAll(notes);
    }

    /**
     * Add a note to the song.
     *
     * @param note the note
     */
    public void addNode(Note note) {
        this.notes.add(note);
    }

    /**
     * Play the song for a Minecraft player.
     *
     * @param player the player
     */
    public void play(Player player) {
        TaskChain<?> chain = SlotMachine.newChain();
        for (int i = 0; i < notes.size(); i++) {
            playNote(player, i, chain);
        }
        chain.execute();
    }

    public void play(Collection<Player> players) {
        TaskChain<?> chain = SlotMachine.newChain();
        for (int i = 0; i < notes.size(); i++) {
            playNote(players, i, chain);
        }
        chain.execute();
    }

    public void play(Player player, TaskChain<?> chain) {
        for (int i = 0; i < notes.size(); i++) {
            playNote(player, i, chain);
        }
    }

    public void play(Collection<Player> players, TaskChain<?> chain) {
        for (int i = 0; i < notes.size(); i++) {
            playNote(players, i, chain);
        }
    }

    public void play(Player player, TaskChain<?> chain, Location location) {
        for (int i = 0; i < notes.size(); i++) {
            playNote(player, i, chain, location);
        }
    }

    public void play(Collection<Player> players, TaskChain<?> chain, Location location) {
        for (int i = 0; i < notes.size(); i++) {
            playNote(players, i, chain, location);
        }
    }

    private void playNote(Player player, int index, TaskChain<?> chain) {
        playNote(player, index, chain, player.getLocation());
    }

    private void playNote(Player player, int index, TaskChain<?> chain, Location location) {
        chain.delay(this.notes.get(index).delay).async(() ->
                player.playSound(location, this.notes.get(index).sound,
                        SoundCategory.AMBIENT, this.notes.get(index).volume, this.notes.get(index).pitch));
    }

    private void playNote(Collection<Player> players, int index, TaskChain<?> chain) {
        players.forEach(player -> playNote(player, index, chain));
    }

    private void playNote(Collection<Player> players, int index, TaskChain<?> chain, Location location) {
        players.forEach(player -> playNote(player, index, chain, location));
    }

    public void playNote(Location location, int index, TaskChain<?> chain) {
        chain.delay(this.notes.get(index).delay).async(() -> {
            final Collection<Player> receivers = new HashSet<>();
            final Set<UUID> viewers = getScreenViewerSet();
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(location.getWorld())) {
                    final double dist = player.getLocation().distanceSquared(location);
                    if (viewers.contains(player.getUniqueId()) && dist > MAX_DIST) {
                        // Remove
                        viewers.remove(player.getUniqueId());
                    } else if (!viewers.contains(player.getUniqueId()) && dist < MAX_DIST) {
                        // Add
                        viewers.add(player.getUniqueId());
                    }
                } else {
                    viewers.remove(player.getUniqueId());
                }
                if (viewers.contains(player.getUniqueId())) {
                    // Update
                    receivers.add(player);
                }
            }
            if (!receivers.isEmpty()) {
                receivers.forEach(player -> player.playSound(location, this.notes.get(index).sound,
                        SoundCategory.AMBIENT, this.notes.get(index).volume, this.notes.get(index).pitch));
            }
            for (final UUID uuid : Set.copyOf(viewers)) {
                if (Bukkit.getPlayer(uuid) == null) {
                    viewers.remove(uuid);
                }
            }
        });
    }

    private final Set<UUID> screenViewerMap = new HashSet<>();

    public Set<UUID> getScreenViewerSet() {
        return screenViewerMap;
    }

    private static final double MAX_DIST = Math.pow(48, 2);

    public void play(Location location) {
        TaskChain<?> chain = SlotMachine.newChain();
        for (int i = 0; i < notes.size(); i++) {
            playNote(location, i, chain);
        }
        chain.execute();
    }

    public void play(Location location, TaskChain<?> chain) {
        for (int i = 0; i < notes.size(); i++) {
            playNote(location, i, chain);
        }
    }

    /**
     * A single sound (note) that can be strung together into a {@link Song}.
     */
    public record Note(Sound sound, float pitch, int delay, float volume) {
    }

}
