package fairteam_backend.gradebook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QuickRosterParseTest {
    @Test
    void readsOneStudentPerLine() {
        assertThat(QuickRosterService.parse("Ana Ruiz\n\n  Luis   Mora  \r\nSara Gil"))
                .extracting(QuickRosterService.Entry::name).containsExactly("Ana Ruiz", "Luis Mora", "Sara Gil");
    }

    @Test
    void acceptsEmailsSeparatedBySemicolonCommaOrTab() {
        assertThat(QuickRosterService.parse("Ana Ruiz; ANA@U.EDU\nLuis Mora, luis@u.edu\nSara Gil\tsara@u.edu"))
                .extracting(QuickRosterService.Entry::email).containsExactly("ana@u.edu", "luis@u.edu", "sara@u.edu");
    }

    @Test
    void keepsCommasThatAreNotFollowedByAnEmail() {
        assertThat(QuickRosterService.parse("Ruiz, Ana").get(0)).isEqualTo(new QuickRosterService.Entry("Ruiz, Ana", null));
    }

    @Test
    void dropsListNumbersAndBareEmails() {
        assertThat(QuickRosterService.parse("1. Ana Ruiz\n2) Luis Mora\nsolo@correo.com"))
                .extracting(QuickRosterService.Entry::name).containsExactly("Ana Ruiz", "Luis Mora");
    }
}
