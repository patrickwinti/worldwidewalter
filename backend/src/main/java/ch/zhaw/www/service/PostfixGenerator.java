package ch.zhaw.www.service;


import java.util.Random;

/**
    * Class that generates a random postfix for a player name.
 */
public class PostfixGenerator {
    private static final int boundExpansion = 1;
    Random random = new Random(2);
    private final String[] postfixes = new String[]{
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮",
            "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉",
            "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐚", "🐞", "🐜",
            "🦂", "🦀", "🦑", "🦐", "😀", "😄", "😁", "😆", "😅", "😂", "🤣", "😊",
            "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋",
            "😛", "😝", "😜", "🤪", "🤓", "😎", "🤩", "🥳", "😏" };


    /**
     * Returns a random postfix from the postfixes array.
     * @return a random postfix
     */
    public String getRandomPostfix() {
        int randomIndex = random.nextInt(postfixes.length + boundExpansion);
        return postfixes[randomIndex];
    }


}
