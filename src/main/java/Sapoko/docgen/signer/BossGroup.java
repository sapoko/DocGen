package Sapoko.docgen.signer;

public enum BossGroup {
    PLATOON_COMMAND("Командование роты"),
    SCHOOL_COMMAND("Командование училища"),
    SERGEANTS("Сержантский состав роты");

    private final String displayName;

    public String getDisplayName() {
        return displayName;
    }

    BossGroup(String displayName) {
        this.displayName = displayName;
    }
}
