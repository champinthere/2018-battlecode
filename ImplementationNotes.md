# Notes on Java BattleCode Implementation

## Action, ActionStatus, ActionChain, ActionQueue
- an Action represents a chain of events to execute
- an Action knows how long it has existed for
- an Action has a Type
- an Action has a description
- an Action has a scope (how many units it effects)
- an Action needs to maintain s
- certain Actions may support modifying scope / pairing with a builder action
 so that builder 
- Actions should terminate
- an Action has a priority