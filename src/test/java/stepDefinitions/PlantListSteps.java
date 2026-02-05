package stepDefinitions;

import io.cucumber.java.en.*;
import org.testng.Assert;
import pages.plants.PlantListPage;
import utils.DriverManager;
import utils.ConfigReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlantListSteps {

    PlantListPage plantListPage;

    @When("admin navigates to the Plant List page")
    public void admin_navigates_to_plant_list() {
        DriverManager.getDriver().get(
                ConfigReader.get("ui.base.url") + ConfigReader.get("ui.plants.path")
        );
        plantListPage = new PlantListPage(DriverManager.getDriver());
    }

    @Then("Low badge should be displayed for plants with quantity less than five")
    public void verify_low_badge() {
        Assert.assertTrue(
                plantListPage.isLowBadgeDisplayedForLowQuantityPlant(),
                "Low badge is NOT displayed for plants with quantity < 5"
        );
    }

    @When("user clicks on the Quantity column header")
    public void user_clicks_on_quantity_header() {
        plantListPage.clickQuantityHeader();
    }

    @Then("the plant list should be sorted by quantity in ascending order")
    public void verify_ascending_quantity_sorting() {
        // 1. Get the actual numbers from the UI
        List<Integer> actualQuantities = plantListPage.getAllQuantities();

        // 2. Create a copy and sort it using Java
        List<Integer> expectedQuantities = new ArrayList<>(actualQuantities);
        Collections.sort(expectedQuantities);

        // 3. Compare
        Assert.assertEquals(actualQuantities, expectedQuantities,
                "Descending sort failed!");
    }

    @Then("the plant list should be sorted by quantity in descending order")
    public void verify_descending_quantity_sorting() {
        List<Integer> actual = plantListPage.getAllQuantities();
        List<Integer> expected = new ArrayList<>(actual);

        // Sort ascending then reverse to get descending
        Collections.sort(expected);
        Collections.reverse(expected);

        Assert.assertEquals(actual, expected, "Descending sort failed!");
    }
}
