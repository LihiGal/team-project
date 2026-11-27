package interface_adapter.end_day;

import use_case.end_day.EndDayInputBoundary;

public class EndDayController {

    private final EndDayInputBoundary endDayInteractor;

    public EndDayController(EndDayInputBoundary endDayInteractor){
        this.endDayInteractor = endDayInteractor;
    }

    public void execute(){
        endDayInteractor.execute();
    }
}
