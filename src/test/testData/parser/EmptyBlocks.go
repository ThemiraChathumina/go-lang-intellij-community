package main

func EmptyBlocks() {
	// Valid Go! Blocks forever.
	select {}

	// Valid Go! Does nothing.
	switch {}

	// Valid Go! Type switch with no cases.
	var i any
	switch i.(type) {}
}