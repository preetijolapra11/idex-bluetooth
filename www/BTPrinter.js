var exec = require('cordova/exec');

var BTPrinter = {
   list: function(fnSuccess, fnError){
      exec(fnSuccess, fnError, "BTPrinter", "list", []);
   },
   connect: function(fnSuccess, fnError, name){
      exec(fnSuccess, fnError, "BTPrinter", "connect", [name]);
   },
   disconnect: function(fnSuccess, fnError){
      exec(fnSuccess, fnError, "BTPrinter", "disconnect", []);
   },
   print: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BTPrinter", "print", [str]);
   },
   printText: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BTPrinter", "printText", [str]);
   },
   printImage: function(fnSuccess, fnError, str, int){
      exec(fnSuccess, fnError, "BTPrinter", "printImage", [str, int]);
   },
   barcode: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BTPrinter", "barcode", [str]);
   },
   printPOSCommand: function(fnSuccess, fnError, str){
      exec(fnSuccess, fnError, "BTPrinter", "printPOSCommand", [str]);
   }
};

module.exports = BTPrinter;
