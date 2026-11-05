import java.util.concurrent.ThreadLocalRandom;


public class Doctor implements Runnable {

    private static final long ROOM_TIMEOUT_MS = 3000;


    private static final int MIN_TREATMENT_TIME_MS = 1000;
    private static final int MAX_TREATMENT_TIME_MS = 5000;

    private final int doctorId;
    private final WaitingRoom waitingRoom;
    private final TreatmentRooms treatmentRooms;

    public Doctor(
            int doctorId,
            WaitingRoom waitingRoom,
            TreatmentRooms treatmentRooms
    ) {
        this.doctorId = doctorId;
        this.waitingRoom = waitingRoom;
        this.treatmentRooms = treatmentRooms;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Patient patient;


            try {
                patient = waitingRoom.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (patient == null) {
                Logger.log("Doctor " + doctorId + " is stopping.");
                return;
            }

            Logger.log("Doctor " + doctorId + " is assigned to " + patient.getPatientName() + ".");

            Logger.log("Doctor " + doctorId + " is trying to acquire a treatment room.");

            boolean roomAcquired = false;
            boolean patientNotified = false;

            try {
                roomAcquired = treatmentRooms.tryAcquire(ROOM_TIMEOUT_MS);

                if (!roomAcquired) {
                    Logger.log("Doctor " + doctorId + " could not acquire a treatment room.");

                    Logger.log("Doctor " + doctorId + " is sending " + patient.getPatientName() + " back to the waiting room.");

                    patient.signalSentBack();
                    patientNotified = true;

                } else {
                    Logger.log("Doctor " + doctorId + " acquired a treatment room.");

                    Logger.log("Doctor " + doctorId + " begins treating " + patient.getPatientName() + ".");

                    int treatmentTime = ThreadLocalRandom.current().nextInt(MIN_TREATMENT_TIME_MS, MAX_TREATMENT_TIME_MS + 1);

                    Thread.sleep(treatmentTime);

                    Logger.log("Doctor " + doctorId + " finished treating " + patient.getPatientName() + ".");

                    patient.signalTreated();
                    patientNotified = true;
                }

            } catch (InterruptedException e) {

                if (!patientNotified) {
                    patient.signalSentBack();
                }

                Thread.currentThread().interrupt();
                return;

            } finally {
                
                if (roomAcquired) {
                    treatmentRooms.release();

                    Logger.log("Doctor " + doctorId + " released the treatment room.");
                }
            }

            Logger.log("Doctor " + doctorId + " is now available.");
        }
    }
}