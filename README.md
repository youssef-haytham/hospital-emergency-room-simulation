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
| `Hospital` | Entry point. Reads and validates the input, creates the shared resources, doctors and patients, waits for all patients, then stops the doctors. |
| `Patient` | A patient thread. Arrives, enters the waiting room, waits for the doctor's outcome, retries if sent back, leaves after treatment. |
| `Doctor` | A doctor thread. Takes a patient, tries to acquire a treatment room within 3 seconds, treats or sends the patient back. |
| `Semaphore` | Custom counting semaphore (blocking, based on the lab), with a timed acquire method. |
| `WaitingRoom` | Bounded buffer of patients, synchronized with semaphores. |
| `TreatmentRooms` | The two treatment rooms, controlled by a semaphore. |
| `Status_printer` | Prints simulation events so that lines from different threads do not mix. |

## Requirements

- JDK 8 or later.
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

Invalid input (text, zero, or negative numbers) is rejected and the prompt is
repeated.

## Example

Input:

```
Waiting room capacity: 2
Number of doctors: 3
Number of patients: 5
```

The beginning of one possible run looks like this (the real order of events
changes from run to run because of thread scheduling):

```
Patients arriving:
P1 P2 P3 P4 P5
P4 arrived.
P4 entered the waiting room.
P5 arrived.
P5 entered the waiting room.
P3 arrived.
The waiting room is full.
P3 is waiting outside for an available seat.
Doctor 2 is assigned to P4.
Doctor 2 is trying to acquire a treatment room.
P3 entered the waiting room.
...
All patients have been treated.
Simulation completed.
```

## How Synchronization Works

- **Waiting room (bounded buffer):** three semaphores protect a shared queue.
  `available_seats` counts free seats, `waiting_patients` counts patients
  sitting in the room, and `queue_lock` (a binary semaphore) gives exclusive
  access to the queue. A patient blocks when there is no free seat, and a doctor
  blocks when there is no patient. The lock is needed because there are many
  producers (patients) and many consumers (doctors).
- **Atomic check and log:** a patient decides "seat free or room full" and prints
  the matching message while holding `queue_lock`, so "The waiting room is
  full." is always true at the moment it is printed. A patient that must wait
  blocks outside the lock, so doctors can still free seats.
- **Seat release:** a doctor takes a patient, then prints "is assigned to" and
  frees the seat in one step (`releaseSeat`). This keeps the log consistent: a
  new patient can only enter after the assignment that freed its seat is printed.
- **Treatment rooms:** a separate semaphore initialized to 2. It is independent
  from the waiting room, as the assignment requires. A doctor prints "released"
  just before releasing the room, so no other doctor's "acquired" line can
  appear first.
- **Doctor-patient communication:** each patient stores the doctor's outcome
  (treated or sent back) inside the patient object. The patient waits with
  `wait()` in a loop that checks this stored value, so a signal sent before the
  patient starts waiting is not lost.

## Deadlock Freedom

- A doctor holds at most one resource at a time while blocking: either it waits
  for a patient, or it waits (at most 3 seconds) for a treatment room. It never
  waits for a room while holding a seat or the queue lock.
- A patient waiting for a seat does not hold `queue_lock`, so doctors can always
  take patients and free seats.
- A sent-back patient re-enters the waiting room on its own thread, so the
  doctor is never blocked by a full waiting room.
- Treatment rooms are always released after use, so a waiting doctor eventually
  gets one or times out.

## Treatment-Room Timeout

A doctor calls a timed acquire on the treatment rooms and waits up to
3 seconds. If no room becomes free in that time, the doctor sends the patient
back, and the patient tries to enter the waiting room again. Because treatment
takes 1 to 5 seconds, this path appears in runs with many patients and few
rooms (for example 2 seats, 5 doctors, 8 patients).

## Treatment Duration

Treatment takes a random time between **1000 ms** and **5000 ms**
(`MIN_TREATMENT_TIME_MS` and `MAX_TREATMENT_TIME_MS` in `Doctor`).

## Termination

The program waits (`join`) for every patient thread. A patient thread ends only
after its treatment is finished, so when all patients have ended nobody needs
treatment. `Hospital` then puts one "no more work" marker (`null`) per doctor in
the waiting room, using the same lock as the patients. Each doctor stops when it
receives a marker, and `Hospital` joins all doctor threads before printing the
final messages, so "Simulation completed." is always the last line.

## Assumptions and Limitations

- Event order differs between runs; only the logical order is guaranteed
  (for example, a patient never leaves before being treated, and at most two
  treatments run at the same time).
- Doctors are stopped with markers rather than `Thread.interrupt()`, which keeps
  shutdown deterministic.
- Patients waiting outside are not served in strict first-come order; a newly
  arriving patient may take a seat that has just been freed.
- Input is read from the console and must be whole numbers.