# Expenses Example
--------------------------------------------

## Meta Data

- A: Does not want to pay for meat.
- B: Does not want to pay for alcohol.
- C: Does not want to pay for meat.
- D: pays for everything


## Scene
--------------------------------------------

- A+B bought alcohol: 10.00 € (A: 7€, B: 3€)
- A+B bought meat: 5.00 € (A: 2.5€, B: 2.5€)
- C+D bought groceries: 50.00€ (A: 5€, B: 10€, C: 15€, D: 20€)

## Entries (Expenses)
--------------------------------------------

- Entry("Alcohol", payers =  (A to 700, B to 300), users/members = (A, C, D))
- Entry("Meat", payers =  (A to 250, B to 250), users/members = (B, C, D))
- Entry("Groceries", payers =  (A to 500, B to 1000, C to 1500, D to 2000), users/members = (A, B, C, D))


## Pay / Balance Day
--------------------------------------------

### Single Payments
--------------------------------------------
- A paid: 7 + 2.5 + 5 = 14.5
- B paid: 3 + 2.5 + 10 = 15.5
- C paid: 15 = 15
- D paid: 20 = 20

### Overall Payments
--------------------------------------------
- 10.00 + 5.00 + 50.00
- = 65.00

- Meat: 0500ct, 2 Payers: 0500.0/2 =  250.000 ct per user
- Alc:  1000ct, 3 payers: 1000.0/3 =  333.333 ct per user
- Misc: 5000ct, 4 payers: 5000.0/4 = 1250.000 ct per user


### Personal Bills:
- D: 250.0 + 333.333 + 1250.0 = 1833.333 ct
- C: 333.333 + 1250.0 = 1583.333 ct
- A: 333.333 + 1250.0 = 1583.333 ct
- B: 250.0 + 1250.0 = 1500.0 ct

### Personal Deficits

- A: 1583.333 - 1450.000 =   133.333 ct
- B: 1500.000 - 1550.000 =   -50.0   ct
- C: 1583.333 - 1500.000 =    83.333 ct
- D: 1833.333 - 2000.000 =  -166.667 ct

- (negative: will be paid back from the group/pot; positive: must be paid / put into the pot)


## Questions / To Do:
1. Rethink the Balance Entry
  - Is the Balance entry still one entry for the whole pay back day?
  - Are all the members members of the Balance entry, but their participation property fetched from each included expense
  - Will the Creation will bring this bill or create a Bill Type wich then just creates the Balance  item (that only checks if balancing amount is zero matches and each member has paid a similar amount +- one cent/or whatever minimal unit)
  - Members are not an own property of the balance entry.
  - Missing Payers in the balance (which were part in either paying or using), are throwing exceptions in creation.
