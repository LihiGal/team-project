package use_case.end_day;

public interface EndDayOutputBoundary {
    void prepareSuccessView(EndDayOutputData outputData);

    void prepareFailView(String error);
}
