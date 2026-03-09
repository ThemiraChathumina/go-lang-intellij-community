package main

// Basic function with variadic args and multiple returns
func ComplexFunc(a int, b ...string) (string, error) {
	return b[0], nil
}

// Named return values
func NamedReturn() (result int) {
	result = 42
	return
}

// Value receiver method
func (u User) GetName() string {
	return u.Name
}

// Pointer receiver method
func (u *User) SetName(name string) {
	u.Name = name
}

// Anonymous immediately invoked function (Closure)
func Closures() {
	defer func(msg string) {
		println(msg)
	}("Cleaning up")
}