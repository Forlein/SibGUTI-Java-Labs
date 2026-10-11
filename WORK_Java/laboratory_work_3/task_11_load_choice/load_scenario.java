package task_11_load_choice;

public class load_scenario {
    private final String scenario_name;
    private final String chosen_structure;
    private final String main_complexity;

    public load_scenario(String scenario_name, String chosen_structure, String main_complexity) {
        this.scenario_name = scenario_name;
        this.chosen_structure = chosen_structure;
        this.main_complexity = main_complexity;
    }

    @Override
    public String toString() {
        return scenario_name + " -> " + chosen_structure + " (" + main_complexity + ")";
    }
}
