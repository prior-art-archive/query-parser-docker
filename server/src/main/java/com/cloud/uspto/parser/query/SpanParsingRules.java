package com.cloud.uspto.parser.query;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.Var;
import org.parboiled.trees.ImmutableBinaryTreeNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.cloud.uspto.parser.query.BoolParsingRules.BoolNode;
import com.cloud.uspto.parser.query.SpanParsingRules.SpanNode;

import com.cloud.uspto.model.ParserConstants;



@BuildParseTree
public class SpanParsingRules extends BaseParser<SpanNode> {
	public Rule InputLine() {
		return Sequence(Expression(), EOI);
	}

    public Rule Expression() {
        Var<String> op = new Var<String>();
        return Sequence(Expr(),
        		ZeroOrMore(
                        OR(), op.set(match()),
                        Expr(),
                        pushWithContext(op)
                ));
    }

    Rule Expr() {
        Var<String> op = new Var<String>();
        return Sequence(
                ExpTerm(),
                ZeroOrMore(
                        XOR(), op.set(match()),
                        ExpTerm(),
                        push(new SpanNode(op.get(), pop(1), pop()))
                        //push (new  CalcCNode.op.get().pop(1),pop()))
                        
                )
        );
    }

     Rule ExpTerm() {
        Var<String> op = new Var<String>();
        return Sequence(
        		SameExp(),
                ZeroOrMore(
                		FirstOf(AND(), NOT()), op.set(match()),
                		SameExp(),
                        push(new SpanNode(op.get(), pop(1), pop()))
                )
        );
    }

     Rule SameExp() {
         Var<String> op = new Var<String>();
         return Sequence(
        		 WithExp(),
                 ZeroOrMore(
                         SAME(), op.set(match()),
                         WithExp(),
                         pushWithContext(op)
                 )
         );
     }

     Rule WithExp() {
         Var<String> op = new Var<String>();
         return Sequence(
                 Term(),
                 ZeroOrMore(
                         WITH(), op.set(match()),
                         Term(),
                         pushWithContext(op)
                 )
         );
     }

    Rule Term() {
        Var<String> op = new Var<String>();
        return Sequence(
                Factor(),
                ZeroOrMore(
                		FirstOf(ADJ(), NEAR(), ONEAR()), op.set(match()),
                		Factor(),
                        pushWithContext(op)
                )
        );
    }
    
    boolean pushWithContext(Var<String> op) {
    	SpanNode left = pop(1);
    	SpanNode right = pop();
    	if(left.hasContextOperator() && !right.hasContextOperator()) {
    		right = new SpanNode(left.operator, right, new SpanNode(""));
    	} else if(right.hasContextOperator() && !left.hasContextOperator()) {
    		left = new SpanNode(right.operator, left, new SpanNode(""));
    	}
    	return push(new SpanNode(op.get(),left, right));
    }

    Rule Factor() {
        Var<String> op = new Var<String>();
        return Sequence(
                Atom(),
                ZeroOrMore(
                		AnyOf("|"), op.set(match()),
                        Regex(),
                        push(new SpanNode(op.get(), pop(1), pop()))
                )
        );
    }
    
    Rule Atom() {
        return Sequence(SpacelessAtom(),
        		Optional(TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), 
        				NonWhiteSpaceAtom(), push(new SpanNode("SPACE", pop(1), pop())))
    			);
    }
       
    
    Rule SpacelessAtom() {
   	 Var<String> op = new Var<String>();
       return Sequence(SubAtom(), ContextOperator(), op.set(match()), push(new SpanNode(op.get(), pop(), new SpanNode(""))));
    }
      
    
    Rule NonWhiteSpaceAtom() {
      	 Var<String> op = new Var<String>();
          return Sequence(NonWhitespaceSubAtom(), ContextOperator(), op.set(match()), push(new SpanNode(op.get(), pop(), new SpanNode(""))));
    }
    
       
    Rule SubAtom() {
        return FirstOf(Parens(), LineNos(), Phrases(), Dates(), Context());
    }
    
        
    Rule NonWhitespaceSubAtom() {
        return FirstOf(Parens(), LineNos(), Phrases(), Dates(), NonWhitespaceContext());
    }
    
     
    Rule LineNos() {
    	return Sequence(
    			LineNo(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new SpanNode("SPACE", pop(1), pop())))
    			);
    }

    Rule LineNo() {
    	return Sequence(
    			Sequence(
    					'L', OneOrMore(Digit())
    					),
    			push(new SpanNode(matchOrDefault(" ")))
    			);
    }

    Rule Parens() {
        return Sequence("(", Expression(), ")", 
        		ZeroOrMore(NonOperatorWhiteSpace(), Expression(),
        				push(new SpanNode("SPACE", pop(1), pop()))));
    }
    Rule NonOperatorWhiteSpace() {
    	 return OneOrMore(TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), AnyOf(" \t\f"));
    }

    Rule Phrases() {
    	return Sequence(
    			Phrase(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new SpanNode("SPACE", pop(1), pop())))
    			);
    }

    Rule Phrase() {
    	return Sequence(
    			Sequence(
    					'"', OneOrMore(TestNot('"'),ANY),
    					ZeroOrMore(TestNot('"'),FirstOf(' ', OneOrMore(ANY))),
    					'"'
    					),
    			push(new SpanNode("ADJ", splitPhrase(match())))
    			);
    }
    
    String[] splitPhrase(String phrase) {
    	return phrase.substring(1, phrase.length()-1).split(" ");
    }

    Rule Dates() {
    	return Sequence(
    			Date(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new SpanNode("SPACE", pop(1), pop())))
    			);
    }

    Rule Date() {
    	return Sequence(
    			FirstOf(
    			Sequence(
    					FirstOf(IgnoreCase("@RLAD"), IgnoreCase("@AD"), IgnoreCase("@PD"), IgnoreCase("@FD"), IgnoreCase("@RLFD"), IgnoreCase("@ISD")),
    	    			WhiteSpace(), FirstOf("<=", ">=", '=', "<>", '<', '>'), WhiteSpace(), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), FirstOf(Sequence('0', Digit()), Sequence('1', FirstOf('0', '1', '2'))),
    	    			FirstOf(Sequence('0', Digit()), Sequence('1', Digit()), Sequence('2', Digit()), Sequence('3', FirstOf('0', '1'))),
    	    			Optional('"'),
    	    			Optional(FirstOf("<=", ">=", '<', '>', '=', "<>"), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), FirstOf(Sequence('0', Digit()), Sequence('1', FirstOf('0', '1', '2'))),
    	    			FirstOf(Sequence('0', Digit()), Sequence('1', Digit()), Sequence('2', Digit()), Sequence('3', FirstOf('0', '1'))),
    	    			Optional('"'))
    			),
    			Sequence(
    					FirstOf(IgnoreCase("@AY"), IgnoreCase("@PY"), IgnoreCase("@FY"), IgnoreCase("@ISY")),
    					WhiteSpace(), FirstOf("<=", ">=", '=', "<>", '<', '>'), WhiteSpace(), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), Optional('"'),
    	    			Optional(FirstOf("<=", ">=", '<', '>', '=', "<>"), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), Optional('"'))
    			)),
    			push(new SpanNode(matchOrDefault(" ")))
    			);
    }
    
    

    
    
    Rule Context() {
        Var<String> op = new Var<String>();
    	return Sequence(
    			Space(),
    			ZeroOrMore(
    					FirstOf(":<", ":>", ":", "=<", "=>", "="), op.set(match()),
    					WhiteSpace(),
    					Expression(),
    					push(new SpanNode(op.get(), pop(1), pop()))
    			));
    }
    
    
    
    Rule NonWhitespaceContext() {
        Var<String> op = new Var<String>();
    	return Sequence(
    			NonWhitespaceSpace(),
    			ZeroOrMore(
    					FirstOf(":<", ":>", ":", "=<", "=>", "="), op.set(match()),
    					WhiteSpace(),
    					Expression(),
    					push(new SpanNode(op.get(), pop(1), pop()))
    			));
    }

    
    

    Rule Space() {
    	return Sequence(
    			Regex(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new SpanNode("SPACE", pop(1), pop())))
    			);
    }
    
    

    Rule NonWhitespaceSpace() {
    	return Sequence(
    			NonWhitespaceRegex(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new SpanNode("SPACE", pop(1), pop())))
    			);
    }
    
    

    Rule Regex() {
    	return Sequence(
    			ZeroOrMore(TestNot(IgnoreCase("NOT ")), TestNot(AnyOf(":=~^<>")), FirstOf(OneOrMore(Alphabet()), AnyOf(",${}?+*"))),
    			push(new SpanNode(matchOrDefault(" ")))
    			);
    }
       
    Rule NonWhitespaceRegex() {
    	return Sequence(
    			OneOrMore(TestNot(IgnoreCase("NOT ")), TestNot(AnyOf(":=~^<>")), FirstOf(OneOrMore(Alphabet()), AnyOf(",${}?+*"))),
    			push(new SpanNode(matchOrDefault(" ")))
    			);
    }

    Rule Alphabet() {
    	return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Digit(), '_', '-','/','^','~');
    }

    Rule Digit() {
        return CharRange('0', '9');
    }
    
    Rule AtLeastOneWhiteSpace() {
        return OneOrMore(AnyOf(" \t\f"));
    }

    Rule WhiteSpace() {
        return ZeroOrMore(AnyOf(" \t\f"));
    }

    Rule AND() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("AND"), WhiteSpace()
    			);
    }
    
    Rule ContextOperator() {
    	return 	Sequence(
    			WhiteSpace(), FirstOf(IgnoreCase(".ab."), IgnoreCase(".ti."), IgnoreCase(".cpc."), IgnoreCase(".ti,ab."), IgnoreCase(".ab,ti."), IgnoreCase("."), ""), WhiteSpace()    			
    			);
    }

    Rule OR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("OR"), WhiteSpace()
    			);
    }

    Rule XOR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("XOR"), WhiteSpace()
    			);
    }

    Rule ADJ() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("ADJ"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }

    Rule NEAR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("NEAR"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }

    Rule ONEAR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("ONEAR"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }

    Rule WITH() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("WITH"), ZeroOrMore(Digit())
    			,WhiteSpace()
    			);
    }
    
    Rule SAME() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("SAME"), ZeroOrMore(Digit())
    			//Optional(Digit()), Optional(Digit())
    			, WhiteSpace()
    			);
    }   
    
    Rule NOT() {
    	return Sequence(
    			//WhiteSpace(), FirstOf(IgnoreCase("NOT"), '-'), WhiteSpace()
    				WhiteSpace(), FirstOf(IgnoreCase("NOT"), '-'), AtLeastOneWhiteSpace()
    			);
    }

    
   
    public class SpanNode extends ImmutableBinaryTreeNode<SpanNode> {
        private Object value;
        private String[] operands;
        private String operator;
        private String distance = "0";
        private boolean contextOperaterNode;

		private String defaultField = "search_content";
        ObjectMapper mapper = new ObjectMapper();
        
		
		public SpanNode(String operator, String[] operands) {
            super(null, null);
            this.operator = operator.toUpperCase().trim();
            this.operands = operands;
        }

		public SpanNode(String value) {
            super(null, null);
            this.value = value;
        }

        public SpanNode(String operator, SpanNode left, SpanNode right) {
            super(left, right);
            operator = operator.toUpperCase().trim();

            if (operator.startsWith("ADJ") && operator.length() > 3) {
            	this.distance = operator.substring(3);
            	this.operator = "ADJ";

            } else if (operator.startsWith("NEAR") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "NEAR";

            } else if (operator.startsWith("ONEAR") && operator.length() > 5) {
            	this.distance = operator.substring(5);
            	this.operator = "ONEAR";
            	
            } else if (operator.startsWith("WITH") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "WITH";


            } else if (operator.startsWith("SAME") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "SAME";

            } else {
            	this.operator = operator;
            }
            if(distance.isEmpty()) {
            	distance = "0";
            }
        }

        
		public Object getValue(String defaultOperator){
	        
	        boolean AND_ORDER = false;
	        boolean isRegex = false;
	        boolean isWildcard = false;
	        long AND_SLOP = 2147483647;

	        Object left;
	        Object right;
	        
	        ObjectNode objectNode;
	        ObjectNode json;
	    	ObjectNode spanNEAR;
	    	ObjectNode spanMULTI;
	    	ObjectNode matchNode;
	    	ObjectNode spanOR;
	    	ObjectNode term;
	    	ObjectNode fieldValue;
	    	JsonNode fieldValueNode;
	    	JsonNode jsonNode;
	        ArrayNode nearClauses;
	        ArrayNode orClauses;
	        JsonNode leftNode;
	    	JsonNode rightNode;
	    	String fuzziness = "0";
			String boostValue = "0";
			
			String strValue = "";
            if (operator == null) {
	            	if( value instanceof String){
	            		strValue = (String)value;
	            	while (strValue.contains("$")) {
	            		if (strValue.endsWith("$")) {
	            			if (strValue.startsWith("$")) {
	            				String str = strValue.substring(1, strValue.length() - 1);
	            				if (!str.contains("$") && str.length() < 4) {
	            					break;
	            				}
	            			}
	            			strValue = strValue.substring(0, strValue.length() - 1) + "*";
	            		}
	            		if (!strValue.contains("$")) {
	            			break;
	            		}
	            		String no = strValue.substring(strValue.indexOf("$")+1, strValue.indexOf("$")+2);
	        			if (no.matches("[0-9]")) {
	        				strValue = strValue.substring(0, strValue.indexOf("$")) + "[a-zA-Z0-9]{0," + no + "}" + strValue.substring(strValue.indexOf("$")+2);
	        				isRegex = true;
	        			} else {
	        				strValue = strValue.substring(0, strValue.indexOf("$")) + "*" + strValue.substring(strValue.indexOf("$")+1);
	        			}
	        			if (!strValue.contains("$") && strValue.contains("{0,")) {
	                		isRegex = true;
	                	}
	        		}
	            	if (strValue.startsWith("^")) {
	            		isRegex = true;
	            	} else if (strValue.endsWith("*") || strValue.endsWith("?")){
	            		isWildcard = true;
	            	}
	            	else if (strValue.startsWith("@")) {
	            		return null;
	            	}else if(strValue.contains("^")){
	            		boostValue = strValue.substring(strValue.indexOf("^")+1);
	            		if(!boostValue.matches("\\d+")){
	            			boostValue = "0";
	            		}
	            		strValue = strValue.substring(0,strValue.indexOf("^"));
	            		objectNode = mapper.createObjectNode();
	            		objectNode.put("value", strValue);
	            		objectNode.put("boost", boostValue);
	            		term = mapper.createObjectNode();
	            		term.replace(defaultField, objectNode);
	            		json = mapper.createObjectNode();
	            		json.replace("span_term",term);
	            		return (JsonNode)json;
	            	}
	            	else if(strValue.contains("~")){
	            		fuzziness = strValue.substring(strValue.indexOf("~")+1);
	            		if(!fuzziness.matches("\\d+")){
	            			fuzziness = "0";
	            		}
	            		strValue = strValue.substring(0,strValue.indexOf("~"));
	            		objectNode = mapper.createObjectNode();
	            		objectNode.put("value", strValue);
	            		objectNode.put("fuzziness", fuzziness);
	            		term = mapper.createObjectNode();
	            		term.replace(defaultField, objectNode);
	            		matchNode = mapper.createObjectNode();
	            		matchNode.replace("fuzzy", term);
	            		spanMULTI = mapper.createObjectNode();
	            		spanMULTI.replace("match", matchNode);
	            		json = mapper.createObjectNode();
	            		json.replace("span_multi",spanMULTI);
	            		return (JsonNode)json;
	            	}
	            	if (isWildcard || isRegex){
	            		fieldValueNode = createFieldValueNode(getDefaultField(), strValue,false);
						objectNode = mapper.createObjectNode();
						matchNode = mapper.createObjectNode();
						spanMULTI = mapper.createObjectNode();
						if(isWildcard){
							 objectNode.replace("wildcard", fieldValueNode);
						}else{
							 objectNode.replace("regexp",fieldValueNode);
						}
						matchNode.replace("match", objectNode);
						spanMULTI.replace("span_multi", matchNode);
						return (JsonNode)spanMULTI;
	            	}
	            	return createFieldValueNode(getDefaultField(), strValue,true);
            	}
	            	return createFieldValueNode(getDefaultField(), "",true);
            }
            left = null;
            if(left() != null){
            	left = left().getValue(defaultOperator);
            }
            right = null;
            if(right() != null){
            	right = right().getValue(defaultOperator);
            }
            if("".equals(operator)) {
            	return left;
            }
            if (operator.equalsIgnoreCase("SPACE")) {
            	this.operator = defaultOperator.toUpperCase();
            } else if (operator.equalsIgnoreCase("|")) {
            	this.operator = "OR";
            }
            if (left!= null && left instanceof String && ((String)left).matches("L[0-9]+")) {
            	return right;
            }
            if (right!= null && right instanceof String && ((String)right).matches("L[0-9]+")) {
            	return left;
            }

            if (operator.equalsIgnoreCase("AND")) {
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
            	
            	if (left instanceof String) {
            		//TBD
            	} else {
            		if(left instanceof JsonNode  ){
            			jsonNode =  ((JsonNode)left).get("span_near");
            			if( jsonNode != null && jsonNode.get("slop")!=null && jsonNode.get("slop").asLong() == AND_SLOP &&  jsonNode.get("in_order") != null && jsonNode.get("in_order").asBoolean() == AND_ORDER ){
		            			ArrayNode clauses =  (ArrayNode) jsonNode.get("clauses");
		            			if(clauses != null ){
		            				nearClauses.addAll(clauses);
		            			}
            			}else{
            				nearClauses.add(((JsonNode)left));
            			}
            		}
            	}
            	if (right instanceof String) {
            		//TBD
            	} else {
            		if(right instanceof JsonNode ){
            			jsonNode =  ((JsonNode)right).get("span_near");
            			if( jsonNode != null && jsonNode.get("slop")!=null && jsonNode.get("slop").asLong() == AND_SLOP &&  jsonNode.get("in_order")!=null && jsonNode.get("in_order").asBoolean() == AND_ORDER ){
		            			ArrayNode clauses =  (ArrayNode) jsonNode.get("clauses");
		            			if(clauses != null ){
		            				nearClauses.addAll(clauses);
		            			}
            			}else{
            				nearClauses.add(((JsonNode)right));
            			}
            		}
            	}
            	if(nearClauses.size() <= 0){
	            	 return null;
            	}
            	
            	spanNEAR.replace("clauses", nearClauses);
           	 	spanNEAR.put("slop", AND_SLOP);
           	 	spanNEAR.put("in_order", AND_ORDER);
                json.replace("span_near",spanNEAR);
                return json;
                 
            } else if (operator.equalsIgnoreCase("OR")) {
            	json = mapper.createObjectNode();
            	spanOR = mapper.createObjectNode();
                orClauses = mapper.createArrayNode();
                
            	if (left instanceof String) {
            		//TBD
            		
            	} else {
            		if(left instanceof JsonNode  ){
            			if(((JsonNode)left).get("span_or") != null){
		            			ArrayNode clauses =  (ArrayNode) ((JsonNode)left).get("span_or").get("clauses");
		            			if(clauses != null ){
		            				orClauses.addAll(clauses);
		            			}
            			}else{
            				orClauses.add(((JsonNode)left));
            			}
            		}
            	}
            	if (right  instanceof String) {
            		//TBD
            	} else {
            		if(right  instanceof JsonNode ){
            			if( ((JsonNode)right).get("span_or") != null){
	            			ArrayNode clauses =  (ArrayNode) ((JsonNode)right).get("span_or").get("clauses");
	            			if(clauses != null ){
	            				orClauses.addAll(clauses);
	            			}
            			}else{
            				orClauses.add(((JsonNode)right));
            			}
            		}
            	spanOR.replace("clauses", orClauses);
                json.replace("span_or",spanOR);
                if(orClauses.size() <= 0){
                	return null;
                }
                return json;
            	}
            } else if (operator.equalsIgnoreCase("XOR")) {
                return "OR(AND(" + left + ", NOT(" + right + ")), AND(NOT(" + left + "), " + right + "))";

            } else if (operator.equalsIgnoreCase("ADJ")) {
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
                
            	if(operands != null) {
            		for(String operand : operands){
                		nearClauses.add(createFieldValueNode(getDefaultField(), operand,true));
            		}
            	} else {
            		if(left instanceof String){
            			//TBD
            		}else{
            			if(left !=null ){
            				nearClauses.add((JsonNode) left);
            			}
            		}
            		
            		if(right instanceof String){
            			//TBD
            		}else{
            			if(right != null){
            				nearClauses.add((JsonNode) right);
            			}
            		}
            	}
            	if(nearClauses.size() <= 0){
            		return null;
            	}
            	spanNEAR.replace("clauses", nearClauses);
        		spanNEAR.put("slop", new Long(distance).longValue());
           	 	spanNEAR.put("in_order", true);
                json.replace("span_near",spanNEAR);
                return json;
                
            } else if (operator.equalsIgnoreCase("NEAR")) {
            	//System.out.println("Entering into NEAR");
            	long slop = 0;
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
                
            	if (!distance.equalsIgnoreCase("0")) {
            		slop = new Long(distance).longValue();
            	}
            	//System.out.println("left :" +left);
            	//System.out.println("right : "+right);
            	if(left instanceof String){
            		//TBD
            	}else{
            		if(left != null){
            			nearClauses.add((JsonNode)left);
            		}
            	}
            	if(right instanceof String){
            		//TBD
            	}else{
            		if( right != null){
            			nearClauses.add((JsonNode)right);
            		}
            	}
            	
            	if(nearClauses.size() <= 0){
            		return null;
            	}
            	
            	spanNEAR.replace("clauses", nearClauses);
        		spanNEAR.put("slop", slop);
           	 	spanNEAR.put("in_order", false);
                json.replace("span_near",spanNEAR);
                return json;
                
            } else if (operator.equalsIgnoreCase("ONEAR")) {
            	long slop = 0;
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
                
            	if (!distance.equalsIgnoreCase("0")) {
            		slop = new Long(distance).longValue();
            	}
            	
            	if(left instanceof String){
            		//TBD
            	}else{
            		if(left != null){
            			nearClauses.add((JsonNode)left);
            		}
            	}
            	if(right instanceof String){
            		//TBD
            	}else{
            		if(right != null){
            			nearClauses.add((JsonNode)right);
            		}
            	}
            	
            	if(nearClauses.size() <= 0){
            		return null;
            	}
            	
            	spanNEAR.replace("clauses", nearClauses);
        		spanNEAR.put("slop", slop);
           	 	spanNEAR.put("in_order", true);
                json.replace("span_near",spanNEAR);
                return json;

            } else if (operator.equalsIgnoreCase("WITH")) {
            	
            	long slop = 15;
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
                
            	if (!distance.equalsIgnoreCase("0")) {
            		slop = new Long(distance).longValue();
            	}
            	
            	if(left instanceof String){
            		//TBD
            	}else{
            		if(left !=null ){
            			nearClauses.add((JsonNode)left);
            		}
            	}
            	if(right instanceof String){
            		//TBD
            	}else{
            		if(right != null){
            			nearClauses.add((JsonNode)right);
            		}
            	}
            	
            	
            	if(nearClauses.size() <= 0){
            		return null;
            	}
            	spanNEAR.replace("clauses", nearClauses);
        		spanNEAR.put("slop", slop);
           	 	spanNEAR.put("in_order", false);
                json.replace("span_near",spanNEAR);
                return json;
            	
            } else if (operator.equalsIgnoreCase("SAME")) {

            	long slop = 200;
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
                
            	if (!distance.equalsIgnoreCase("0")) {
            		slop = new Long(distance).longValue();
            	}
            	
            	if(left instanceof String){
            		//TBD
            	}else{
            		if(left != null){
            			nearClauses.add((JsonNode)left);
            		}
            	}
            	if(right instanceof String){
            		//TBD
            	}else{
            		if(right != null){
            			nearClauses.add((JsonNode)right);
            		}
            	}
            	
            	if(nearClauses.size() <= 0){
            		return null;
            	}
            	
            	spanNEAR.replace("clauses", nearClauses);
        		spanNEAR.put("slop", slop);
           	 	spanNEAR.put("in_order", false);
                json.replace("span_near",spanNEAR);
                return json;

            } else if (operator.equalsIgnoreCase("NOT") || operator.equalsIgnoreCase("-")) {
            	return (left != " " ? left : null);

            } else if (operator.contains(":") || operator.contains("=")) {
            	json = mapper.createObjectNode();
            	spanNEAR = mapper.createObjectNode();
                nearClauses = mapper.createArrayNode();
                nearClauses.add(((JsonNode)right));
                nearClauses.add(((JsonNode)left));
                spanNEAR.replace("clauses", nearClauses);
                spanNEAR.put("slop", AND_SLOP);
           	 	spanNEAR.put("in_order", AND_ORDER);
                json.replace("span_near",spanNEAR);
			
            	return json;
            	
            }else if(this.hasContextOperator())  {
            	/*
            	json = null;
            	JsonNode termNode;
            	String value = null;
            	leftNode = ((JsonNode)left);
            	rightNode = ((JsonNode)right);
            	if(leftNode !=null && leftNode.has("span_term")){
            		termNode = leftNode.get("span_term");
            		if(termNode.has(defaultField)){
            			value = termNode.get(defaultField).asText();
            			if(operator.equalsIgnoreCase(".ab.")){
	            			fieldValue = mapper.createObjectNode();
	            			fieldValue.put(ParserConstants.DESCRIPTION,value);
	    					json = mapper.createObjectNode();
	    					json.replace("span_term", fieldValue);
	    					return json;
            			}else if(operator.equalsIgnoreCase(".ti.")){
            				fieldValue = mapper.createObjectNode();
            				fieldValue.put(ParserConstants.CUSTOM_META_DATA_TITLE,value);
	    					json = mapper.createObjectNode();
	    					json.replace("span_term", fieldValue);
            			}
            			else if(operator.equalsIgnoreCase(".cpc.")){
            				fieldValue = mapper.createObjectNode();
            				fieldValue.put(ParserConstants.CPC_CODES,value);
	    					json = mapper.createObjectNode();
	    					json.replace("span_term", fieldValue);
            			}
            			else if(operator.equalsIgnoreCase(".ab,ti.") || operator.equalsIgnoreCase(".ti,ab.")){
            				json = mapper.createObjectNode();
            				spanOR = mapper.createObjectNode();
            				orClauses = mapper.createArrayNode();
            				term = mapper.createObjectNode();
            				fieldValue = mapper.createObjectNode();
            				fieldValue.put(ParserConstants.CUSTOM_META_DATA_TITLE,value);
	    					term = mapper.createObjectNode();
	    					term.replace("span_term", fieldValue);
	    					orClauses.add(term);
	    					
	    					term = mapper.createObjectNode();
	    					fieldValue = mapper.createObjectNode();
	    					fieldValue.put(ParserConstants.DESCRIPTION,value);
	    					term = mapper.createObjectNode();
	    					term.replace("span_term", fieldValue);
	    					orClauses.add(term);
	    					
	    					spanOR.replace("caluses", orClauses);
	    					json.replace("span_or", spanOR);
            			}
            		}
            	}
            	return json;
            	*/  return null;
            } 
            else {
            	//System.out.println("LEFT: " + left);
            	//System.out.println("OP: " + operator);
            	//System.out.println("RIGHT: " + right);
                throw new IllegalStateException();
            }
			return distance;
			
        }
		boolean hasContextOperator() {
			return operator.equalsIgnoreCase(".ab.") || operator.equalsIgnoreCase(".ti.") || operator.equalsIgnoreCase(".cpc.") || 
					operator.equalsIgnoreCase(".ab,ti.") || operator.equalsIgnoreCase(".ti,ab.");
		}
      
       @Override
        public String toString() {
            try {
				return mapper.writeValueAsString(getValue(this.operator));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return "";
        }
        
        public JsonNode createFieldValueNode(String field, String value,boolean nodeAsTerm){
        	if(value !=null && !value.trim().isEmpty()){
        		ObjectNode node = mapper.createObjectNode();
            	node.put(field, value);
            	
            	if(nodeAsTerm){
            		ObjectNode termNode = mapper.createObjectNode();
            		termNode.replace("span_term", node);
            		return (JsonNode)termNode;
            	}
            	return (JsonNode)node;
            	
        	}
        	return null;
        }

		public  String getDefaultField() {
			return defaultField;
		}

		public  void setDefaultField(String defaultField) {
			defaultField = defaultField;
		}

		public boolean isContextOperaterNode() {
			return contextOperaterNode;
		}

		public void setContextOperaterNode(boolean contextOperaterNode) {
			this.contextOperaterNode = contextOperaterNode;
		}
    }
}
