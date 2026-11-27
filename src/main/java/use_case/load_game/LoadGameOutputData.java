package use_case.load_game;

import entity.GameStatus;

public record LoadGameOutputData(int day, int totalDays, int energy, int sanity,
                                 int academicSurvival, int money, int remainingTimeBlocks,
                                 GameStatus status) {
}
