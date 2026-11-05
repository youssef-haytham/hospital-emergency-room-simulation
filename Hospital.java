import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Hospital {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Waiting room capacity: ");
        int capacity = scanner.nextInt();
        System.out.println("Number of doctors: ");
        int doctorCount = scanner.nextInt();
        System.out.println("Number of patients: ");
        int patientCount = scanner.nextInt();

        WaitingRoom waitingRoom = new WaitingRoom(capacity);
        TreatmentRooms treatmentRooms = new TreatmentRooms();

        // from here the code needs more review 

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

        Status_printer.print_status("Patients arriving:");
        Status_printer.print_status(names.toString());

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

            // for (Thread doctorThread : doctorThreads) {
            // doctorThread.join();
            // }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted.");
            scanner.close();
            return;
        }

        Status_printer.print_status("All patients have been treated.");
        Status_printer.print_status("Simulation completed.");

        scanner.close();
    }
}
