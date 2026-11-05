import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the simulation. Reads the input, creates the shared
 * resources, doctors and patients, and shuts everything down cleanly.
 */
public class Hospital {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int capacity = readPositiveInt(scanner, "Waiting room capacity: ");
        int doctorCount = readPositiveInt(scanner, "Number of doctors: ");
        int patientCount = readPositiveInt(scanner, "Number of patients: ");

        // Two independent shared resources (as the assignment requires).
        WaitingRoom waitingRoom = new WaitingRoom(capacity);
        TreatmentRooms treatmentRooms = new TreatmentRooms();

        // Doctors first, so they are already idle when patients arrive.
        List<Thread> doctorThreads = new ArrayList<>();
        for (int i = 1; i <= doctorCount; i++) {
            Doctor doctor = new Doctor(i, waitingRoom, treatmentRooms);
            Thread thread = new Thread(doctor);
            doctorThreads.add(thread);
            thread.start();
        }

        // Create all patients first so the "Patients arriving" line is complete.
        List<Patient> patients = new ArrayList<>();
        StringBuilder names = new StringBuilder();
        for (int i = 1; i <= patientCount; i++) {
            Patient patient = new Patient(i, waitingRoom);
            patients.add(patient);
            if (i > 1) {
                names.append(" ");
            }
            names.append(patient.getPatientName());
        }

        Logger.log("Patients arriving:");
        Logger.log(names.toString());

        for (Patient patient : patients) {
            patient.start();
        }

        try {
            // A patient thread ends only after it has been treated and has left,
            // so when every join() returns, no patient needs treatment anymore.
            for (Patient patient : patients) {
                patient.join();
            }

            // One poison pill per doctor wakes up the blocked ones.
            waitingRoom.closeFor(doctorCount);

            for (Thread doctorThread : doctorThreads) {
                doctorThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted.");
            return;
        }

        Logger.log("All patients have been treated.");
        Logger.log("Simulation completed.");
    }

    /** Keeps asking until the user enters a whole number >= 1. */
    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= 1) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }
}
