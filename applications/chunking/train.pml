include "corpora/train.types.pml";

include "conll00.pml";
include "chunking.pml";
include "tagging.pml";
include "global.pml";
include "joint.pml";

load global from "global.txt";
load global.rare from "corpora/rare.txt";

set instancesCacheSize = 3;

load corpus from conll00 "corpora/train.conll";

//load weights from dump "/tmp/blank.weights.dmp";
load weights from dump "/tmp/weights.dmp";

load instances from dump "/tmp/instances.dmp";

set learner.solver.ilp.solver = "lpsolve";
//set learner.solver.ilp.solver.timeout = 100;
//set learner.solver.ilp.solver.bbDepthLimit = 5;
//set learner.solver.ilp.solver.implementation = "clp";
set learner.solver.maxIterations = 10;
set learner.solver.integer = true;
set learner.solver.deterministicFirst = false;
set learner.update = "mira";
set learner.update.signs = true;
set learner.maxCandidates = 10;
set learner.average = true;
set learner.maxViolations = 1;
set learner.loss = "globalNumErrors";
set learner.profile = true;
//set learner.

learn for 60 epochs;

save weights to dump "/tmp/weights.dmp";

print learner.profiler;