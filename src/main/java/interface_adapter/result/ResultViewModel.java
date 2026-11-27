package interface_adapter.result;

import interface_adapter.ViewModel;

public class ResultViewModel extends ViewModel<ResultState> {

    public ResultViewModel(){
        super("result");
        setState(new ResultState());
    }
}
