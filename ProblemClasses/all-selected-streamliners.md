# CVRP

- |plan| / 2 = sum([toInt(1 >= sum([toInt(q23 < 1 + (numberLocations - 1) / 2) | q23 <- range(q13)])) | q13 <- plan])


# CarSequencing

- |range(car)| / 2 + 1 >= sum([toInt(q11 > 1 + (n_classes - 1) / 2) | q11 <- range(car)]) /\ |range(car)| / 2 - 1 <= sum([toInt(q11 > 1 + (n_classes - 1) / 2) | q11 <- range(car)])


# EFPA

- and([and([image(q1, min(defined(q1))) <= image(q1, q6) | q6 <- defined(q1)]) | q1 <- c])
- and([|range(q1)| / 2 = sum([toInt(q10 % 2 = 0) | q10 <- range(q1)]) | q1 <- c])"},
- 1 >= sum([toInt(and([and([q38 < q39 -> image(q35, q38) >= image(q35, q39) | q39 <- defined(q35)]) | q38 <- defined(q35)])) | q35 <- c])
- 1 >= sum([toInt(and([q43 < 1 + (numChars - 1) / 2 | q43 <- range(q35)])) | q35 <- c])


# GracefulDoubleWheelGraphs

- |range(edgeID)| / 2 = sum([toInt(q25 % 2 = 1) | q25 <- range(edgeID)])
- |range(edgeID)| / 2 = sum([toInt(q25 % 2 = 0) | q25 <- range(edgeID)])
- |range(edgeID)| / 2 = sum([toInt(q25 > 1 + (4 * n - 1) / 2) | q25 <- range(edgeID)])
- |range(edgeID)| / 2 + 1 >= sum([toInt(q27 > 1 + (4 * n - 1) / 2) | q27 <- range(edgeID)]) /\ |range(edgeID)| / 2 - 1 <= sum([toInt(q27 > 1 + (4 * n - 1) / 2) | q27 <- range(edgeID)])


# GracefulGears

- |range(colour)| / 2 = sum([toInt(q9 < 1 + (3 * n - 1) / 2) | q9 <- range(colour)])
- |range(colour)| / 2 + 1 >= sum([toInt(q11 < 1 + (3 * n - 1) / 2) | q11 <- range(colour)]) /\ |range(colour)| / 2 - 1 <= sum([toInt(q11 < 1 + (3 * n - 1) / 2) | q11 <- range(colour)])
- |range(colour)| / 2 + 1 >= sum([toInt(q11 > 1 + (3 * n - 1) / 2) | q11 <- range(colour)]) /\ |range(colour)| / 2 - 1 <= sum([toInt(q11 > 1 + (3 * n - 1) / 2) | q11 <- range(colour)])
- and([image(edgeID, min(defined(edgeID))) <= image(edgeID, q21)          | q21 <- defined(edgeID)])


# GracefulHelms

- |range(colouring)| / 2 + 1 >= sum([toInt(q11 > 1 + (3 * n - 1) / 2) | q11 <- range(colouring)]) /\ |range(colouring)| / 2 - 1 <= sum([toInt(q11 > 1 + (3 * n - 1) / 2) | q11 <- range(colouring)])
- |range(edgeID)| / 2 + 1 >= sum([toInt(q27 % 2 = 1) | q27 <- range(edgeID)]) /\ |range(edgeID)| / 2 - 1 <= sum([toInt(q27 % 2 = 1) | q27 <- range(edgeID)])
- |range(edgeID)| / 2 + 1 >= sum([toInt(q27 % 2 = 0) | q27 <- range(edgeID)]) /\ |range(edgeID)| / 2 - 1 <= sum([toInt(q27 % 2 = 0) | q27 <- range(edgeID)])


# GracefulWheelGraphs

- and([image(colouring, min(defined(colouring))) <= image(colouring, q5) | q5 <- defined(colouring)])
- and([image(colouring, max(defined(colouring))) >= image(colouring, q6) | q6 <- defined(colouring)])
- |range(edgeID)| / 2 + 1 >= sum([toInt(q27 < 1 + (2 * n - 1) / 2) | q27 <- range(edgeID)]) /\ |range(edgeID)| / 2 - 1 <= sum([toInt(q27 < 1 + (2 * n - 1) / 2) | q27 <- range(edgeID)])
- |range(edgeID)| / 2 + 1 >= sum([toInt(q27 > 1 + (2 * n - 1) / 2) | q27 <- range(edgeID)]) /\ |range(edgeID)| / 2 - 1 <= sum([toInt(q27 > 1 + (2 * n - 1) / 2) | q27 <- range(edgeID)])


# PPP

- |hosts| / 2 = sum([toInt(q2 % 2 = 0) | q2 <- hosts])
- and([and([and([q6 < q7 -> image(q5, q6) <= image(q5, q7) | q7 <- defined(q5)]) | q6 <- defined(q5)]) | q5 <- sched])
- and([|defined(q5)| / 2 + 1 >= sum([toInt(q21 > 1 + (n_boats - 1) / 2) | q21 <- defined(q5)]) /\ |defined(q5)| / 2 - 1 <= sum([toInt(q21 > 1 + (n_boats - 1) / 2) | q21 <- defined(q5)]) | q5 <- sched])
- |sched| / 2 = sum([toInt(1 >= sum([toInt(q32 < 1 + (n_boats - 1) / 2) | q32 <- range(q22)])) | q22 <- sched])


# SchursLemma

- |parts(boxes)| / 2 = sum([toInt(1 >= sum([toInt(q10 % 2 = 1) | q10 <- q7])) | q7 <- parts(boxes)])
- |parts(boxes)| / 2 = sum([toInt(1 >= sum([toInt(q10 < 1 + (n - 1) / 2) | q10 <- q7])) | q7 <- parts(boxes)])
- |parts(boxes)| / 2 + 1 >= sum([toInt(and([q18 < 1 + (n - 1) / 2 | q18 <- q17])) | q17 <- parts(boxes)]) /\ |parts(boxes)| / 2 - 1 <= sum([toInt(and([q18 < 1 + (n - 1) / 2 | q18 <- q17])) | q17 <- parts(boxes)])

# VanDerWaerden

- and([|q2| / 2 = sum([toInt(q4 % 2 = 0) | q4 <- q2]) | q2 <- parts(p)])
- and([|q2| / 2 = sum([toInt(q4 < 1 + (n - 1) / 2) | q4 <- q2]) | q2 <- parts(p)])
- and([|q2| / 2 = sum([toInt(q4 > 1 + (n - 1) / 2) | q4 <- q2]) | q2 <- parts(p)])
- minPartSize(p, |participants(p)| / |parts(p)| - 1) /\ maxPartSize(p, |participants(p)| / |parts(p)| + 1)

# MEB

- 1 >= sum([toInt(q10 < 1 + (numberNodes - 1) / 2) | q10 <- range(parents)])
- and([q24 < 1 + (numberNodes - 1) / 2 | q24 <- range(depths)])
- |range(depths)| / 2 = sum([toInt(q25 % 2 = 1) | q25 <- range(depths)])
