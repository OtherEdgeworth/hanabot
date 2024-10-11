public enum ClueType
{
    NULL,
    PLAY,
    FIVE_SAVE,
    TWO_SAVE,
    CRITICAL_SAVE,
    DELAYED_PLAY;

    public boolean isSaveClue() { return this == FIVE_SAVE || this == TWO_SAVE || this == CRITICAL_SAVE; }
}
