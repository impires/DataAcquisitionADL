package phdsound.ubi.com.phdsoundfingerprinting;

/**
 * Created by Ivan on 18/12/15.
 */
public class Variables {
    /**
     * Variable for start capture time
     */
    public static long timeCaptureStart;

    /**
     * Variable for end capture time
     */
    public static long timeCaptureEnd;

    /**
     * Variable for each folder path
     */
    public static String folderPath;

    /**
     * Variable for the time before start the capture
     */
    public static int TIME_BEFORE = 3000;

    /**
     * Variable for the duration time for each capture
     */
    public static int TIME_DEFAULT = 5000;

    /**
     * Variable for waiting time for each capture (alterado para facilitar os testes - 3 segundos)
     */
    public static int TIME_LOOP = 30 * 1000;

    /**
     * Variable to define the ADL selected
     */
    public static String ADL_SELECTED = "undifferentiated";
    public static int POS_ADL_SELECTED = 0;

}
