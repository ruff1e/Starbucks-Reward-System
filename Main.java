import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Main {
    // Arrays for regular customers and for preferred customers.
    static Customer[] regular;
    static Customer[] preferred;
    // For preferred customers we also keep a copy for the "regular" view (which applies the discount on orders).
    static Customer[] prefRegular;

    // Reads the regular customer file.
    public static Customer[] readRegular(String filename) {
        try {
            File file = new File(filename);
            Scanner sc = new Scanner(file);
            int count = 0;
            while(sc.hasNext()) {
                sc.next(); // guestID
                sc.next(); // first name
                sc.next(); // last name
                sc.nextDouble(); // amount
                if(sc.hasNextLine()) sc.nextLine();
                count++;
            }
            sc.close();
            Customer[] arr = new Customer[count];
            sc = new Scanner(file);
            int i = 0;
            while(sc.hasNext()) {
                String id = sc.next();
                String first = sc.next();
                String last = sc.next();
                double amt = sc.nextDouble();
                if(sc.hasNextLine()) sc.nextLine();
                arr[i++] = new Customer(first, last, id, amt);
            }
            sc.close();
            return arr;
        } catch (FileNotFoundException e) {
            System.out.println("Regular customer file not found: " + filename);
            return new Customer[0];
        }
    }

    // Reads the preferred customer file.
    // Each record is: guestID firstName lastName amount discountOrBonus
    public static Customer[] readPreferred(String filename) {
        try {
            File file = new File(filename);
            Scanner sc = new Scanner(file);
            int count = 0;
            while(sc.hasNext()) {
                sc.next(); // guestID
                sc.next(); // first name
                sc.next(); // last name
                sc.nextDouble(); // amount
                sc.next(); // discount or bonus
                if(sc.hasNextLine()) sc.nextLine();
                count++;
            }
            sc.close();
            Customer[] arr = new Customer[count];
            sc = new Scanner(file);
            int i = 0;
            while(sc.hasNext()) {
                String id = sc.next();
                String first = sc.next();
                String last = sc.next();
                double amt = sc.nextDouble();
                String discOrBonus = sc.next();
                if(sc.hasNextLine()) sc.nextLine();
                if(discOrBonus.charAt(discOrBonus.length()-1) == '%') {
                    double disc = Double.parseDouble(discOrBonus.substring(0, discOrBonus.length()-1));
                    arr[i++] = new Gold(first, last, id, amt, disc);
                } else {
                    int bonus = Integer.parseInt(discOrBonus);
                    arr[i++] = new Platinum(first, last, id, amt, bonus);
                }
            }
            sc.close();
            return arr;
        } catch (FileNotFoundException e) {
            System.out.println("Preferred customer file not found: " + filename);
            return new Customer[0];
        }
    }

    // Simple method to copy an array.
    public static Customer[] copyArray(Customer[] arr) {
        Customer[] copy = new Customer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

    // Finds a customer by guestID in an array.
    public static Customer findById(String id, Customer[] arr) {
        for (Customer c : arr) {
            if (c.getGuestID().equals(id))
                return c;
        }
        return null;
    }

    // Removes a customer (by id) from an array.
    public static Customer[] removeCustomer(Customer[] arr, String id) {
        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if(arr[i].getGuestID().equals(id)) {
                index = i;
                break;
            }
        }
        if (index == -1) return arr;
        Customer[] newArr = new Customer[arr.length - 1];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (i == index) continue;
            newArr[j++] = arr[i];
        }
        return newArr;
    }

    // Adds a customer to an array.
    public static Customer[] addCustomer(Customer[] arr, Customer c) {
        Customer[] newArr = new Customer[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i];
        }
        newArr[arr.length] = c;
        return newArr;
    }

    // Calculates order price.
    // Cup sizes: S=12oz, M=20oz, L=32oz.
    // Drink prices (per ounce): frap = 0.20, tea = 0.12, latte = 0.15, punch = 0.15, soda = 0.20.
    // Personalization cost = lateral surface area (π * diameter * height) * squareInchPrice.
    public static double calcOrderPrice(char size, String drink, double sqPrice, int qty) {
        double oz = 0, diam = 0, h = 0;
        if (size == 'S' || size == 's') { oz = 12; diam = 4; h = 4.5; }
        else if (size == 'M' || size == 'm') { oz = 20; diam = 4.5; h = 5.75; }
        else if (size == 'L' || size == 'l') { oz = 32; diam = 5.5; h = 7; }
        double pricePerOunce = 0;
        if (drink.equalsIgnoreCase("frap"))
            pricePerOunce = 0.20;
        else if (drink.equalsIgnoreCase("tea"))
            pricePerOunce = 0.12;
        else if (drink.equalsIgnoreCase("latte"))
            pricePerOunce = 0.15;
        else if (drink.equalsIgnoreCase("punch"))
            pricePerOunce = 0.15;
        else if (drink.equalsIgnoreCase("soda"))
            pricePerOunce = 0.20;
        else
            throw new IllegalArgumentException("Invalid drink: " + drink);
        double drinkCost = oz * pricePerOunce;
        double lateral = Math.PI * diam * h;
        double persCost = lateral * sqPrice;
        return (drinkCost + persCost) * qty;
    }

    // Returns the Gold discount percentage based on amount.
    public static double getGoldDiscount(double amt) {
        if (amt < 100) return 5.0;
        else if (amt < 150) return 10.0;
        else if (amt < 200) return 15.0;
        return 0.0;
    }

    // Processes orders from file.
    // For a regular customer: update normally; if amount >= 50, promote them (and remove from regular).
    // For a preferred customer: update their preferred record with full order cost and update the preferred "regular copy"
    // with the order cost after discount.
    public static void processOrders(String filename) {
        try {
            File file = new File(filename);
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                String id = sc.next();
                char size = sc.next().charAt(0);
                String drink = sc.next();
                double sqPrice = sc.nextDouble();
                int qty = sc.nextInt();
                if (sc.hasNextLine()) sc.nextLine();
                double cost = calcOrderPrice(size, drink, sqPrice, qty);
                // Check if customer is in regular array.
                Customer custReg = findById(id, regular);
                if (custReg != null) {
                    custReg.setAmountSpent(custReg.getAmountSpent() + cost);
                    // If reached promotion threshold, remove from regular and add to preferred.
                    if (custReg.getAmountSpent() >= 50) {
                        if (custReg.getAmountSpent() >= 200) {
                            int bonus = (int)((custReg.getAmountSpent() - 200) / 5);
                            regular = removeCustomer(regular, id);
                            preferred = addCustomer(preferred, new Platinum(custReg.getName(), custReg.getLastName(), custReg.getGuestID(), custReg.getAmountSpent(), bonus));
                        } else {
                            double disc = getGoldDiscount(custReg.getAmountSpent());
                            regular = removeCustomer(regular, id);
                            preferred = addCustomer(preferred, new Gold(custReg.getName(), custReg.getLastName(), custReg.getGuestID(), custReg.getAmountSpent(), disc));
                        }
                    }
                } else {
                    // Customer is in preferred.
                    Customer custPref = findById(id, preferred);
                    if (custPref != null) {
                        custPref.setAmountSpent(custPref.getAmountSpent() + cost);
                        // Update the corresponding record in prefRegular with discounted order.
                        Customer custPrefReg = findById(id, prefRegular);
                        if (custPrefReg != null && custPrefReg instanceof Gold) {
                            double disc = ((Gold)custPrefReg).getDiscountPercentage();
                            custPrefReg.setAmountSpent(custPrefReg.getAmountSpent() + cost * (1 - disc/100));
                        }
                        // If a Gold customer reaches 200, promote to Platinum.
                        if (custPref instanceof Gold && custPref.getAmountSpent() >= 200) {
                            int bonus = (int)((custPref.getAmountSpent() - 200) / 5);
                            // Update both preferred arrays.
                            for (int i = 0; i < preferred.length; i++) {
                                if (preferred[i].getGuestID().equals(id)) {
                                    preferred[i] = new Platinum(custPref.getName(), custPref.getLastName(), custPref.getGuestID(), custPref.getAmountSpent(), bonus);
                                }
                            }
                            for (int i = 0; i < prefRegular.length; i++) {
                                if (prefRegular[i].getGuestID().equals(id)) {
                                    prefRegular[i] = new Platinum(custPref.getName(), custPref.getLastName(), custPref.getGuestID(), prefRegular[i].getAmountSpent(), bonus);
                                }
                            }
                        }
                    }
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("Orders file not found: " + filename);
        }
    }

    // Writes the preferred array to a file.
    public static void writePreferredFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(filename);
            for (Customer c : preferred) {
                if (c instanceof Gold) {
                    Gold g = (Gold)c;
                    pw.printf("%s %s %s %.2f %.0f%%\n", g.getGuestID(), g.getName(), g.getLastName(), g.getAmountSpent(), g.getDiscountPercentage());
                } else if (c instanceof Platinum) {
                    Platinum p = (Platinum)c;
                    pw.printf("%s %s %s %.2f %d\n", p.getGuestID(), p.getName(), p.getLastName(), p.getAmountSpent(), p.getBonusBuck());
                }
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("Error writing preferred file: " + filename);
        }
    }

    // Writes the regular file as the union of the remaining regular customers and the preferred regular copy.
    public static void writeRegularFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(filename);
            for (Customer c : regular) {
                pw.printf("%s %s %s %.2f\n", c.getGuestID(), c.getName(), c.getLastName(), c.getAmountSpent());
            }
            for (Customer c : prefRegular) {
                if (c instanceof Gold) {
                    Gold g = (Gold)c;
                    pw.printf("%s %s %s %.2f %.0f%%\n", g.getGuestID(), g.getName(), g.getLastName(), g.getAmountSpent(), g.getDiscountPercentage());
                } else if (c instanceof Platinum) {
                    Platinum p = (Platinum)c;
                    pw.printf("%s %s %s %.2f %d\n", p.getGuestID(), p.getName(), p.getLastName(), p.getAmountSpent(), p.getBonusBuck());
                } else {
                    pw.printf("%s %s %s %.2f\n", c.getGuestID(), c.getName(), c.getLastName(), c.getAmountSpent());
                }
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("Error writing regular file: " + filename);
        }
    }

    public static void main(String[] args) {
        Scanner cons = new Scanner(System.in);
        System.out.print("Enter regular customer file: ");
        String regFile = cons.nextLine();
        System.out.print("Enter preferred customer file: ");
        String prefFile = cons.nextLine();
        System.out.print("Enter orders file: ");
        String ordersFile = cons.nextLine();

        regular = readRegular(regFile);
        preferred = readPreferred(prefFile);
        prefRegular = copyArray(preferred);  // keep a copy for the regular file

        processOrders(ordersFile);

        writePreferredFile("preferred.dat");
        writeRegularFile("customer.dat");
    }
}
