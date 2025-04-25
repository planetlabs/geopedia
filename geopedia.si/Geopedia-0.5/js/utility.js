function preEvaluateScript(jsScript, scale, pixSize) {
	var options = {
			mangle_options: {
				mangle: true,
				defines: {}
			},
			squeeze_options: {
				dead_code: true,
			},
			gen_options: {},
		};
	options.mangle_options.defines["scale"]= [ "num" , scale ];
	options.mangle_options.defines["pixSize"]= [ "num" , pixSize ];
	return uglify(jsScript, options);
}

/*
 * Extracts and returns a comma separated containing the following identifiers:
 * f<id>
 * f<id>.f<id>
 * id<id>
 * reptext<id>
 */
function getIdentifiers(code, preEvaluationVariables) {
	  var metaFields = '(id|u|d|reptext|area)\\d+';
	  var regexpBase = 'f\\d+(_f\\d+)*(_'+metaFields+'){0,1}';	  
	  var regexpPreEval = "scale";
	  var regexpPattern;
	  if (preEvaluationVariables) {
		  regexpPattern="^("+metaFields+"|"+regexpBase+"|"+regexpPreEval+")$";
	  } else {
		  regexpPattern="^("+metaFields+"|"+regexpBase+")$";
	  }
	  var pattern=new RegExp(regexpPattern,"i");
	  var options = { tokens: true};
	  var identifiers={};
	  var tokenizer = uglify.parser.tokenizer(code);
	  var token = tokenizer();
	  while (token.type!='eof') {
	  	if (token.type=='name' && pattern.exec(token.value)) {
	  		identifiers[token.value]=1;
	  	}
	  	token = tokenizer();
	  }
	  return Object.keys(identifiers).join(',');
}


var sf = {};

function createLineSymbolizer(properties) {
	var symb = sf.newLineSymbolizer();
	applyObjectProperties(symb,properties);
	return symb;
}


function createPointSymbolizer(properties) {
	var symb = sf.newPointSymbolizer();
	applyObjectProperties(symb,properties);
	return symb;
}

function createFillSymbolizer(properties) {
	var symb = sf.newFillSymbolizer();
	applyObjectProperties(symb,properties);
	return symb;
}

function createTextSymbolizer(properties) {
	var symb = sf.newTextSymbolizer();
	applyObjectProperties(symb,properties);
	return symb;	
}

function createSymbolizerFont(properties) {
	var fs = sf.newSymbolizerFont();
	applyObjectProperties(fs,properties);
	return fs;
}


function applyObjectProperties(object, properties) {
	for (var prop in properties) {
		var value = properties[prop];
		object[prop] = value;
	}
}

function createSimpleSymbology(symbolizerArray) {	
	var symbology = sf.newSymbology([sf.newPaintingPass(symbolizerArray)]);
	return symbology;
}

function createSymbology(paintingPassesArray) {
	return sf.newSymbology(paintingPassesArray);
}

function createPaintingPass(symbolizersArray) {
	return sf.newPaintingPass(symbolizersArray);
}


sf['LineSymbolizer'] = createLineSymbolizer;
sf['PointSymbolizer'] = createPointSymbolizer;
sf['FillSymbolizer'] = createFillSymbolizer;
sf['TextSymbolizer'] = createTextSymbolizer;
sf['simpleSymbology'] = createSimpleSymbology;
sf['Symbology'] = createSymbology;
sf['PaintingPass'] = createPaintingPass;
sf['SymbolizerFont'] = createSymbolizerFont;




function colorMap(value, defaultColor, limits, colors) {
	if (limits.length!=colors.length) return defaultColor;
	for (var i=0;i<limits.length;i++) {
		if (value<limits[i]) return colors[i];
	}
	return defaultColor;
}


function colorBlend(value, limits, colors) {
	var prev = limits[0];
	if (value <= prev)
		return colors[0];
	
	for (var a=1; a<limits.length; a++) {
		var next = limits[a];
		if (value <= next) {
			var at = (value - prev) / (next - prev);
			var re = 1.0 - at;
			
			var argb0 = colors[a-1];
			var argb1 = colors[a];
			
			var alpha = parseInt((0.5 + re * ((argb0 >>> 24)       ) + at * ((argb1 >>> 24)       )), 10);
			var red   = parseInt((0.5 + re * ((argb0 >>> 16) & 0xFF) + at * ((argb1 >>> 16) & 0xFF)), 10);
			var green = parseInt((0.5 + re * ((argb0 >>>  8) & 0xFF) + at * ((argb1 >>>  8) & 0xFF)), 10);
			var blue  = parseInt((0.5 + re * ((argb0       ) & 0xFF) + at * ((argb1       ) & 0xFF)), 10);
			
			return (alpha << 24) | (red << 16) | (green << 8) | blue;
		} else {
			prev = next;
		}
	}
	return colors[colors.length-1];	
}

function log10(value) {
	return Math.log(value)/Math.LN10;
}
