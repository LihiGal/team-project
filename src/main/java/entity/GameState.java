package entity;

import java.util.UUID;

/**
 * Everything about one game in progress. apply(Outcome) is the single place
 * where an action changes the game, so every rule (clamping, losing, and
 * later time blocks, weather and end of day) is written exactly once.
 */
public class GameState {

    public static final int TOTAL_DAYS = 7;
    public static final String START_LOCATION_ID = "residence";
    public static final int TIME_BLOCK_PER_DAY = 3;

    private final UUID runId;
    private int day;
    private final Player player;
    private String currentLocationId;
    private GameStatus status;
    private int remainingTimeBlocks;

    public GameState(int day, Player player, String currentLocationId) {
        this(UUID.randomUUID(), day, player, currentLocationId, GameStatus.IN_PROGRESS);
    }

    public GameState(UUID runId, int day, Player player, String currentLocationId, GameStatus status){
        this(runId, day, player, currentLocationId, status, TIME_BLOCK_PER_DAY);
    }

    public GameState(UUID runId, int day, Player player, String currentLocationId, GameStatus status, int remainingTimeBlocks) {
        this.runId = runId;
        this.day = day;
        this.player = player;
        this.currentLocationId = currentLocationId;
        this.status = status;
        this.remainingTimeBlocks = remainingTimeBlocks;
    }

    /** A new game: Day 1, starting stats, at the Residence. */
    public static GameState startNew() {
        return new GameState(1, Player.createNew(), START_LOCATION_ID);
    }

    /**
     * Applies an action's result to the game and returns the new status.
     * Later stories extend the body (time blocks, items, weather, end of day);
     * the signature stays the same so callers never change.
     */
    public GameStatus apply(Outcome outcome) {
        if (status == GameStatus.WON || status == GameStatus.LOST) {
            return status;
        }
        player.applyEffects(outcome.getEffects());
        if (outcome.getNewLocationId() != null) {
            currentLocationId = outcome.getNewLocationId();
        }
        if (player.isOutOfStat()) {
            status = GameStatus.LOST;
            return status;
        }
        remainingTimeBlocks = Math.max(0, remainingTimeBlocks - outcome.getTimeBlocksUsed());
        if(remainingTimeBlocks == 0){
            return startNextDay();
        }
        return status;
    }

    public GameStatus startNextDay(){
        if (status != GameStatus.IN_PROGRESS){
            return status;
        }

        if (player.isOutOfStat()){
            status = GameStatus.LOST;
            return status;
        }

        player.applyEffects(new StatEffects(20, 0, 0 , 0));

        if (day >= TOTAL_DAYS){
            if (player.getAcademicSurvival() >= 60){
                status = GameStatus.WON;
            }else{
                status = GameStatus.LOST;
            }
            return status;
        }

        day++;
        remainingTimeBlocks = TIME_BLOCK_PER_DAY;
        currentLocationId = START_LOCATION_ID;

        return status;
      }

    public int getDay() {
        return day;
    }

    public UUID getRunId() {
        return runId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getRemainingTimeBlocks(){return remainingTimeBlocks;}
}
