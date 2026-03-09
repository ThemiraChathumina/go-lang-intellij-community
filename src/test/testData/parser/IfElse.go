package main

func IfElse() {
	// Simple statement + condition
	if a := 1; a > 0 {
		println(a)
	} else if b := 2; b > a { // Else If with its own simple statement
		println(b)
	} else if a == -1 {       // Else If with ONLY a condition
		println("negative")
	} else {                  // Standard Else
		println("done")
	}
}