# Carcassonne — CS-108 Project (EPFL)

This repository contains a Java implementation of Carcassonne (named ChaCuN), built 
as part of the CS-108 “Practice of Object-Oriented Programming” course at EPFL. The goal is 
to combine core game logic with a graphical user interface and network/messaging 
support following the project specification in https://cs108.epfl.ch/archive/24/archive.html

---

## Project Context & Objectives

- Use of collections (lists, sets, maps)
- Java generics and polymorphism
- Design of modular architecture (packages, modules, separation of logic vs UI)
- GUI development (JavaFX)
- Messaging layers (for remote plays)
- Testing and modular integration

This Carcassonne implementation aligns with the project steps starting with tile and 
feature logic, building up board and adjacency, adding game
state and scoring, then UI and messaging.

## Code Structure & Modules

|  Package             | Responsibility                                                                                  |
|----------------------|-------------------------------------------------------------------------------------------------|
| `ch.epfl.chacun`     | Core game logic: tile classes, feature modeling, game state, scoring                            |
| `ch.epfl.chacun.gui` | Graphical interface: rendering the board, handling user input, managing tile placement visually |
| `ch.epfl.cs108`      | Course-provided scaffolding                                                                     |
| `ch.epfl.sigcheck`   | Signature checking                                                                              |
| `test/*`             | Test directory (unit and GUI testing)                                                           |

**Entry point:**  
The GUI-launching main class is at  
`ch.epfl.chacun.gui.Main`

This class sets up the UI, accepts user interaction, and drives the game loop.

---

## Prerequisites

- Java 17 or newer
- JavaFX SDK 22
