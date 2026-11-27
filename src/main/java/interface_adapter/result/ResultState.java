package interface_adapter.result;

public class ResultState {

    private boolean won;
    private String message = "";

    public boolean isWon(){
        return won;
    }

    public void setWon(boolean won){
        this.won = won;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }
}
