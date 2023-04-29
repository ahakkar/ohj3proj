package fi.Sisu.datasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;

public class KoriJSONParserTest {
    
    @Test
    public void parseCourseTest() {
        KoriJSONParser parser = new KoriJSONParser();

        Course c = parser.parseCourse(KoriAPIRequester.requestCourseInfo("uta-ykoodi-47926"));

        assertEquals("Johdatus analyysiin", c.getName());
        assertEquals("MATH.MA.110", c.getAbbreviation());
        assertEquals(5, c.getTargetCredits());
    }

    /**
     * Tests that the parseDegreeProgramme method (and entire request/parse
     * pipeline) works with a few different degree programmes.
     * @param groupId Degree programme's groupId, used to request the degree programme.
     * @param name Degree programme's name.
     * @param code Degree programme's code.
     * @param credits Degree programme's number of credits.
     */
    @ParameterizedTest
    @MethodSource("DegreeProgrammeProvider")
    public void parseDegreeProgrammeTestParametrized(String groupId, String name, String code, int credits) {
        KoriJSONParser parser = new KoriJSONParser();

        SisuNode dp = parser.parseModule(KoriAPIRequester.requestModuleInfo(groupId));
        
        assertEquals(groupId, dp.getGroupId());
        assertEquals(name, dp.getName());
        assertEquals(code, dp.getAbbreviation());
        assertEquals(credits, dp.getTargetCredits());
        assertTrue(dp.getChildModuleIds().size() + dp.getChildCourses().size() > 0);
    }

    static Stream<Arguments> DegreeProgrammeProvider() {
        return Stream.of(
            Arguments.of("tut-dp-g-1242", "Master's Programme in Electrical Engineering", "ELEM", 120),
            Arguments.of("otm-9ef2bbd6-e562-4709-9ccd-87db819e60d2", "Lastentautien erikoislääkärikoulutus (55/2020)", "MEDALATEL2020", 0),
            Arguments.of("uta-tohjelma-1793", "Työn ja hyvinvoinnin maisteriohjelma", "THVM", 120),
            Arguments.of("otm-09d9556e-9144-4373-85b2-86f504f896d7", "Informaatioteknologian ja viestinnän tiedekunnan yleinen tohtoriohjelma", "DPGITC", 240),
            Arguments.of("uta-tohjelma-1770", "Sosiaalityön maisteriohjelma", "STYM", 120),
            Arguments.of("otm-24b4aa6c-4533-40a8-8189-a51d83f5cc2b", "Akuuttilääketieteen erikoislääkärikoulutus (56/2015)", "MEDAAKUEL2015", 0),
            Arguments.of("otm-d9c15834-09db-41fe-af67-ef07a2048d5f","Hallintotieteiden, kauppatieteiden ja politiikan tutkimuksen tohtoriohjelma, lisensiaatin tutkinto", "LPHKP", 150),
            Arguments.of("uta-tohjelma-1777", "Logopedian kandidaattiohjelma", "LOGK", 180),
            Arguments.of("uta-tohjelma-1688", "Politiikan tutkimuksen maisteriohjelma", "POLM", 120),
            Arguments.of("otm-82e1f1f8-f0ef-48f0-9854-f4f7837d9955", "Viestinnän monitieteinen kandidaattiohjelma", "VIMK", 180)
        );
    }

    /**
     * Tests that the parseStudyModule method (and entire request/parse
     * pipeline) works with a few different study modules.
     * @param groupId Study module's groupId, used to request the study module.
     * @param name Study module's name.
     * @param code Study module's code.
     * @param credits Study module's number of credits.
     */
    @ParameterizedTest
    @MethodSource("StudyModuleProvider")
    public void parseStudyModuleTestParametrized(String groupId, String name, String code, int credits) {
        KoriJSONParser parser = new KoriJSONParser();

        SisuNode sm = parser.parseModule(KoriAPIRequester.requestModuleInfo(groupId));
        
        assertEquals(groupId, sm.getGroupId());
        assertEquals(name, sm.getName());
        assertEquals(code, sm.getAbbreviation());
        assertEquals(credits, sm.getTargetCredits());
        assertTrue(sm.getChildModuleIds().size() + sm.getChildCourses().size() > 0);

        for (SisuNode node : sm.getChildModules()){
            for (String childId : node.getChildCourses().keySet()) {
                System.out.println(childId + " " + sm.getChildCourses().get(childId).getName());
            }
        }

        assertTrue(true);
    }

    static Stream<Arguments> StudyModuleProvider() {
        return Stream.of(
            Arguments.of("uta-ok-ykoodi-41155", "Sosiaalityön syventävät opinnot", "STY-S01", 100),
            Arguments.of("otm-ec4c5eee-15c9-4bc4-b704-454f778e7381", "Gastroenterologian erikoislääkärikoulutus", "MEDAGAE", 0),
            Arguments.of("otm-a18be719-68f4-48d0-91c7-754889b512c5", "Hyvinvointipolitiikan ja yhteiskunnan tutkimuksen maisteriohjelman syventävät opinnnot", "HYMST", 90)    
        );
    }

    /**
     * Tests that the RecurseCompositeRules method works for a Module that has 
     * unpredictably nested CompositeRules.
     */
    @Test
    public void RecurseCompositeRulesTest() {
        KoriJSONParser parser = new KoriJSONParser();

        SisuNode sm = parser.parseModule(KoriAPIRequester.requestModuleInfo("otm-81da6bec-f97e-4fca-aca9-a9acaf03f765"));

        assertEquals(7, sm.getChildCourses().size());
        assertEquals(1, sm.getMandatoryCourses().size());
        assertEquals(0, sm.getChosenCourses().size());
        assertEquals(0, sm.getChildModuleIds().size());
        assertEquals("Yhteiskuntatutkimuksen teoriat, ideat ja käytännöt", sm.getName());
    }
}
