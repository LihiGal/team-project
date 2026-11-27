package entity;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurviveWeekTest {

    @Test
    void newGameStartsWithThreeTimeBlocks(){
        GameState game = GameState.startNew();

        assertEquals(1, game.getDay());
        assertEquals(3, game.getRemainingTimeBlocks());
    }

    @Test
    void actionConsumesOneTimeBlock(){
        GameState game = GameState.startNew();

        Outcome outcome = new Outcome.Builder()
                .timeBlocksUsed(1)
                .build();

        game.apply(outcome);

        assertEquals(2, game.getRemainingTimeBlocks());
        assertEquals(1, game.getDay());
    }

    @Test
    void threeTimeBlocksAutomaticallyEndsDay(){
        GameState game = GameState.startNew();

        Outcome outcome = new Outcome.Builder()
                .timeBlocksUsed(1)
                .build();

        game.apply(outcome);
        game.apply(outcome);
        game.apply(outcome);

        assertEquals(2, game.getDay());
        assertEquals(3, game.getRemainingTimeBlocks());
    }

    @Test
    void sleepingRestoresEnergy(){
        Player player = new Player(50,100, 50, 50);
        GameState game = new GameState(1, player, "bahen");

        game.startNextDay();

        assertEquals(70, player.getEnergy());
    }

    @Test
    void sleepingDoesNotExceedMaximumEnergy(){
        Player player = new Player(90,100, 50, 50);
        GameState game = new GameState(1, player, "bahen");

        game.startNextDay();

        assertEquals(100, player.getEnergy());
    }

    @Test
    void nextDayRestoresLocationAndTimebBocks(){
        GameState game = new GameState(1, Player.createNew(), "bahen");

        game.apply(new Outcome.Builder()
                .timeBlocksUsed(1)
                .build());

        game.startNextDay();

        assertEquals(2, game.getDay());
        assertEquals(3, game.getRemainingTimeBlocks());
        assertEquals("residence", game.getCurrentLocationId());
    }

    @Test
    void AcademicSurvivalOfSixtyWinsAfterDaySeven(){
        Player player = new Player(100, 100, 60, 50);
        GameState game = new GameState(7, player, "residence");

        game.startNextDay();

        assertEquals(GameStatus.WON, game.getStatus());
        assertEquals(7, game.getDay());
    }

    @Test
    void AcademicSurvivalBelowSixtyLosesAfterDaySeven(){
        Player player = new Player(100, 100, 59, 50);
        GameState game = new GameState(7, player, "residence");

        game.startNextDay();

        assertEquals(GameStatus.LOST, game.getStatus());
    }

    @Test
    void zeroEnergyCausesImmediateLoss(){
        GameState game = GameState.startNew();

        game.apply(new Outcome.Builder()
                .effects(new StatEffects(-100, 0, 0, 0))
                .timeBlocksUsed(3)
                .build());

        assertEquals(GameStatus.LOST, game.getStatus());
        assertEquals(1, game.getDay());
        assertEquals(0, game.getPlayer().getEnergy());
    }

    @Test
    void zeroSanityCausesImmediateLoss(){
        GameState game = GameState.startNew();

        game.apply(new Outcome.Builder()
                .effects(new StatEffects(0, -100, 0, 0))
                .build());

        assertEquals(GameStatus.LOST, game.getStatus());
    }

    @Test
    void zeroAcademicSurvivalCasuesImmediateLoss(){
        GameState game = GameState.startNew();

        game.apply(new Outcome.Builder()
                .effects(new StatEffects(0, 0, -50, 0))
                .build());

        assertEquals(GameStatus.LOST, game.getStatus());
    }

    @Test
    void finishedGameCannotAdvanceToNextDay(){
        Player player = new Player(100, 100, 60, 50);
        GameState game = new GameState(7, player, "residence");

        game.startNextDay();
        game.startNextDay();

        assertEquals(GameStatus.WON, game.getStatus());
        assertEquals(7, game.getDay());
    }
}
