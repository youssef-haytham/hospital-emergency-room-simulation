# Hospital Emergency Room Simulation

A multithreaded Java simulation of a hospital emergency room, built for
CS241/SCS241 Operating Systems (Assignment 1).

## Purpose

Patients arrive at a hospital with a limited waiting room. Doctors take patients
from the waiting room and need one of only two treatment rooms before they can
start treatment. The project shows how threads can share limited resources
safely, without race conditions or deadlocks.

## Operating Systems Concepts Demonstrated

- Threads (one per patient, one per doctor)
- Producer-Consumer problem with a bounded buffer (the waiting room)
- Counting semaphores (a custom implementation based on the lab)
- Mutual exclusion and thread cooperation (`synchronized`, `wait()`, `notifyAll()`)
- Timed resource acquisition (treatment-room timeout)
- Clean thread termination

## Classes

| Class | Responsibility |
|---|---|
| `Hospital` | Entry point. Reads the input, creates the shared resources, doctors and patients, waits for all patients, then stops the doctors. |
| `Patient` | A patient thread. Arrives, enters the waiting room, waits for the doctor's outcome, retries if sent back, leaves after treatment. |
| `Doctor` | A doctor thread. Takes a patient, tries to acquire a treatment room within 3 seconds, treats or sends the patient back. |
| `Semaphore` | Custom counting semaphore (blocking, based on the lab), with a timed acquire method. |
| `WaitingRoom` | Bounded buffer of patients, synchronized with semaphores. |
| `TreatmentRooms` | The two treatment rooms, controlled by a semaphore. |
| `Status_printer` | Prints simulation events one line at a time so lines do not mix. |

## Requirements

- JDK 8 or later (**TODO: confirm the version the team compiled with**).
- No external libraries.

## Compile and Run

From the project folder:

```
javac *.java
java Hospital
```

## Input

| Prompt | Meaning | Valid values |
|---|---|---|
| `Waiting room capacity:` | Seats in the waiting room | whole number >= 1 |
| `Number of doctors:` | Doctor threads to create | whole number >= 1 |
| `Number of patients:` | Patient threads to create | whole number >= 1 |

Invalid input is rejected and the prompt is repeated.

## Example

Input:

```
Waiting room capacity: 2
Number of doctors: 3
Number of patients: 5
```

The beginning of the output looks like this (taken from the assignment's
sample; the real order of events changes from run to run because of thread
scheduling):

```
Patients arriving:
P1 P2 P3 P4 P5
P1 arrived.
P1 entered the waiting room.
Doctor 1 is assigned to P1.
Doctor 1 is trying to acquire a treatment room.
...
All patients have been treated.
Simulation completed.
```

## How Synchronization Works

- **Waiting room:** a bounded buffer protected by two semaphores. One counts free
  seats and one counts waiting patients. A patient blocks when there is no free
  seat, and a doctor blocks when there is no patient.
- **Treatment rooms:** a separate semaphore initialized to 2. It is independent
  from the waiting room.
- **Doctor-patient communication:** each patient stores the doctor's outcome
  (treated or sent back) inside the patient object. The patient waits with
  `wait()` in a loop that checks this stored value, so a signal sent before the
  patient starts waiting is not lost.

## Treatment-Room Timeout

A doctor calls a timed acquire on the treatment rooms and waits up to
3 seconds. If no room becomes free in that time, the doctor sends the patient
back, and the patient tries to enter the waiting room again.

## Treatment Duration

Treatment takes a random time between **[TODO: MIN]** and **[TODO: MAX]**
(confirm with the team and copy the constants from `Doctor`).

## Termination

The program waits (`join`) for every patient thread. A patient thread ends only
after its treatment is finished, so when all patients have ended nobody needs
treatment. `Hospital` then puts one "no more work" marker per doctor in the
waiting room. Each doctor stops when it receives the marker, and `Hospital`
joins all doctor threads before printing the final messages.

## Assumptions and Limitations

- Event order differs between runs; only the logical order is guaranteed.
- Doctors are stopped with markers, not with `Thread.interrupt()`, because the
  lab's semaphore ignores interrupts.
- Input is read from the console and must be whole numbers.
