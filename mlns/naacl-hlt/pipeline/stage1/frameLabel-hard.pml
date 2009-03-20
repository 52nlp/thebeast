/*
 * The following formulas ensure consistency for frameLabel:
 *          if isArgument it has to had a frameLabel
 */

factor[0]: for Int a               if word(a,_) & possiblePredicate(a) : |FrameLabel f: frameLabel(a,f)| >= 1;
//factor[0]: for Int a, FrameLabel f if word(a,_) & possiblePredicate(a) : frameLabel(a,f) => isPredicate(a);
factor   : for Int a               if word(a,_) & possiblePredicate(a) : |FrameLabel f: frameLabel(a,f)| <= 1;
