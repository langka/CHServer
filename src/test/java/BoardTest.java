import org.junit.Test;
import sse.xs.logic.Game;

/**
 * Created by xusong on 2018/1/8.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class BoardTest {
    @Test
    public void testReset(){
        Game game =new Game(null);
        game.resetPieces();
        game.printChessBoardInConsole();

    }
}
