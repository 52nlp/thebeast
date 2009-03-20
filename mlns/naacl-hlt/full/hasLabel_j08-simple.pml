
// Johansson 2008 (Shared task)


weight w_h_predParentWord: Word -> Double;
factor: for Int a, Int p, Int j, Word l if
  possiblePredicate(p) & possibleArgument(a) & mst_link(j,p) & word(j,l) add[hasLabel(p,a)] * w_h_predParentWord(l);

//weight w_h_predParentLemma: Lemma -> Double;
//factor: for Int a, Int p, Int j, Lemma l if
//  possiblePredicate(p) & possibleArgument(a) & mst_link(j,p) & lemma(j,l) add[hasLabel(p,a)] * w_h_predParentLemma(l);

weight w_h_predParentPos: Ppos -> Double;
factor: for Int a, Int p, Int j, Ppos l if
  possiblePredicate(p) & possibleArgument(a) & mst_link(j,p) & ppos(j,l) add[hasLabel(p,a)] * w_h_predParentPos(l);

weight w_h_childDepSet: DepSet -> Double;
factor: for Int a, Int p, DepSet l if
  possiblePredicate(p) & possibleArgument(a) & childDepSet(p,l) add[hasLabel(p,a)] * w_h_childDepSet(l);

//weight w_h_predLemmaSense: Lemma x FrameLabel -> Double;
//factor: for Int a, Int p, Role r, Lemma l, FrameLabel s if
//  lemma(p,l) & possiblePredicate(p) & possibleArgument(a) & frameLabel(p,s) add[hasLabel(p,a)] * w_h_predLemmaSense(r,l,s);

weight w_h_predLemma: Lemma -> Double;
factor: for Int a, Int p, Role r, Lemma l if
  lemma(p,l) & possiblePredicate(p) & possibleArgument(a) add[hasLabel(p,a)] * w_h_predLemma(l);

//weight w_h_predSense: FrameLabel -> Double;
//factor: for Int a, Int p, FrameLabel l, Role r if
//  possiblePredicate(p) & possibleArgument(a) & frameLabel(p,l) add[hasLabel(p,a)] * w_h_predSense(l);


weight w_h_voice: Voice -> Double;
factor: for Int a, Int p, Voice v if
  possiblePredicate(p) & possibleArgument(a) & voice(p,v) add[hasLabel(p,a)] * w_h_voice(v);

weight w_h_pos_before: Lemma -> Double;
factor: for Int a, Int p, Lemma l if
  possiblePredicate(p) & possibleArgument(a) & lemma(a,l) & a<p add[hasLabel(p,a)] * w_h_pos_before(l);

weight w_h_pos_after: Lemma -> Double;
factor: for Int a, Int p, Lemma l if
  possiblePredicate(p) & possibleArgument(a) & lemma(a,l) & a>p add[hasLabel(p,a)] * w_h_pos_after(l);

weight w_h_pos_equal: Lemma -> Double;
factor: for Int a, Int p, Lemma l if
  possiblePredicate(p) & possibleArgument(a) & lemma(a,l) & a==p add[hasLabel(p,a)] * w_h_pos_equal(l);

weight w_h_argWord: Word -> Double;
factor: for Int a, Int p, Word l if
  possiblePredicate(p) & possibleArgument(a) & word(a,l) add[hasLabel(p,a)] * w_h_argWord(l);

//weight w_h_argLemma: Lemma -> Double;
//factor: for Int a, Int p, Lemma l if
//  possiblePredicate(p) & possibleArgument(a) & lemma(a,l) add[hasLabel(p,a)] * w_h_argLemma(l);

weight w_h_argPpos: Ppos -> Double;
factor: for Int a, Int p, Ppos l if
  possiblePredicate(p) & possibleArgument(a) & ppos(a,l) add[hasLabel(p,a)] * w_h_argPpos(l);

weight w_h_rigthWord:  Word -> Double;
factor: for Int a, Int p, Word l, Int t if
  possiblePredicate(p) & possibleArgument(a) & rightToken(a,t) & word(t,l)  add[hasLabel(p,a)] * w_h_rigthWord(l);

//weight w_h_rigthLemma:  Lemma -> Double;
//factor: for Int a, Int p, Lemma l, Int t if
//  possiblePredicate(p) & possibleArgument(a) & rightToken(a,t) & lemma(t,l)  add[hasLabel(p,a)] * w_h_rigthLemma(l);

weight w_h_rigthPos: Ppos -> Double;
factor: for Int a, Int p, Ppos l, Int t if
  possiblePredicate(p) & possibleArgument(a) & rightToken(a,t) & ppos(t,l)  add[hasLabel(p,a)] * w_h_rigthPos(l);

weight w_h_predPos: Ppos -> Double;
factor: for Int a, Int p, Ppos l if
  possiblePredicate(p) & possibleArgument(a) & ppos(p,l) add[hasLabel(p,a)] * w_h_predPos(l);

weight w_h_relPath: RelPath -> Double;
factor: for Int a, Int p, RelPath l if
  possiblePredicate(p) & possibleArgument(a) & relPath(p,a,l) add[hasLabel(p,a)] * w_h_relPath(l);

weight w_h_verbChainHasSubj: Lemma -> Double;
factor: for Int a, Int p, Lemma l  if
  possiblePredicate(p) & possibleArgument(a) & lemma(a,l) & verbChainHasSubj(p) add[hasLabel(p,a)] * w_h_verbChainHasSubj(l);

weight w_h_controllerHasObj: Lemma -> Double;
factor: for Int a, Int p, Lemma l if
  possiblePredicate(p) & possibleArgument(a) & lemma(a,l) & controllerHasObj(p) add[hasLabel(p,a)] * w_h_controllerHasObj(l);

weight w_h_predRelToParent: MDependency  -> Double;
factor: for Int a, Int p, MDependency l if
  possiblePredicate(p) & possibleArgument(a) & mst_dep(_,p,l) add[hasLabel(p,a)] * w_h_predRelToParent(l);

