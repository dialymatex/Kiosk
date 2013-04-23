import com.dlohaiti.dlokiosk.Measurements;
import com.dlohaiti.dlokiosk.Reading;
import com.dlohaiti.dlokiosk.ReadingValidator;
import org.junit.Before;
import org.junit.Test;

import static com.dlohaiti.dlokiosk.MeasurementLocation.BOREHOLE;
import static com.dlohaiti.dlokiosk.Measurements.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ReadingValidatorTest {

    private ReadingValidator validator;


    private void expectValid(Measurements measurementType, String... values) {
        for (String value : values) {
            Reading reading = new Reading(measurementType, value, BOREHOLE);
            boolean passed = validator.validate(reading);
            assertThat(passed, is(true));
        }
    }

    private void expectInvalid(Measurements measurementType, String... values) {
        for (String value : values) {
            Reading reading = new Reading(measurementType, value, BOREHOLE);
            boolean passed = validator.validate(reading);
            assertThat(passed, is(false));
        }
    }

    @Before
    public void setUp() {
        validator = new ReadingValidator();
    }

    @Test
    public void shouldValidateTemperature() {
        expectInvalid(TEMPERATURE, "9.9", "30.1");
        expectValid(TEMPERATURE, "10", "15", "30");
    }

    @Test
    public void shouldValidatePh() {
        expectInvalid(PH, "4.9", "9.1");
        expectValid(PH, "5", "7", "9");
    }

    @Test
    public void shouldValidateTurbidity() {
        expectInvalid(TURBIDITY, "-0.1", "10.1");
        expectValid(TURBIDITY, "0", "5.5", "10");
    }

    @Test
    public void shouldValidateTds() {
        expectInvalid(TDS, "99.9", "800.1");
        expectValid(TDS, "100", "500", "800");
    }

    @Test
    public void shouldValidateFreeChlorineConcentration() {
        expectInvalid(FREE_CHLORINE_CONCENTRATION, "4999.9", "9000.1");
        expectValid(FREE_CHLORINE_CONCENTRATION, "5000", "7000", "9000");
    }

    @Test
    public void shouldValidateTotalChlorineConcentration() {
        expectInvalid(TOTAL_CHLORINE_CONCENTRATION, "4999.9", "10000.1");
        expectValid(TOTAL_CHLORINE_CONCENTRATION, "5000", "8000", "10000");
    }

    @Test
    public void shouldValidateFreeChlorineResidual() {
        expectInvalid(FREE_CHLORINE_RESIDUAL, "-0.1", "1.1");
        expectValid(FREE_CHLORINE_RESIDUAL, "0", "0.4", "1");
    }

    @Test
    public void shouldValidateTotalChlorineResidual() {
        expectInvalid(TOTAL_CHLORINE_RESIDUAL, "-0.1", "1.1");
        expectValid(TOTAL_CHLORINE_RESIDUAL, "0", "0.4", "1");
    }

    @Test
    public void shouldValidateAlkalinity() {
        expectInvalid(ALKALINITY, "99.9", "500.1");
        expectValid(ALKALINITY, "100", "250", "500");
    }

    @Test
    public void shouldValidateHardness() {
        expectInvalid(HARDNESS, "99.9", "700.1");
        expectValid(HARDNESS, "100", "375", "700");
    }


}
