// Zyklomatische Komplexität

public class Kitchen {

    enum Flavor {
        SWEET,
        SALTY
    }

    enum MainIngredient {
        APPLES,
        CHEESE,
        BANANAS,
        CHERRIES,
        STRAWBERRIES,
        HAM,
        CHICKEN,
        PUMPKIN
    }

    static void cook(MainIngredient mainIngredient, Flavor flavor) {
        System.out.println("Flavor: " + flavor + "\nMain Ingredient: " + mainIngredient);
        System.out.println("Result: ");

        if (flavor == Flavor.SALTY) {
            switch (mainIngredient) {
                case CHEESE -> System.out.println("Mac and Cheese");
                case HAM -> System.out.println("Sandwich");
                case CHICKEN -> System.out.println("Chicken Wings");
                case PUMPKIN -> System.out.println("Pumpkin Soup");
                case STRAWBERRIES -> System.out.println("Strawberry Feta Salad");
                default -> System.out.println("You can't cook a salty dish with your main ingredient.");
            }
        } else if (flavor == Flavor.SWEET) {
            switch (mainIngredient) {
                case APPLES -> System.out.println("Apple Pie");
                case CHEESE -> System.out.println("Cheese Cake");
                case BANANAS -> System.out.println("Banana Bread");
                case CHERRIES -> System.out.println("Cherry Muffins");
                case STRAWBERRIES -> System.out.println("Strawberry Yogurt");
                case PUMPKIN -> System.out.println("Pumpkin Latte");
                default -> System.out.println("You can't cook a sweet dish with your main ingredient.");
            }
        }
    }

    public static void main(String[] args) {
        // Nur Beispiele:
        Flavor flavor = Flavor.SWEET;
        MainIngredient mainIngredient = MainIngredient.CHICKEN;
        cook(mainIngredient, flavor);
    }
}
