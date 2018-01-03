package sse.xs.game;

/**
 * Created by xusong on 2018/1/3.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class GameController {
    private static GameController instance = new GameController();

    private GameController() {

    }

    public static GameController getInstance(){
        return instance;
    }
}
