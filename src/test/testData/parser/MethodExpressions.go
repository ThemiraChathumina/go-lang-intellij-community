package main

type User struct{ Name string }
func (u *User) GetName() string { return u.Name }

func MethodExpressions() {
	// 1. Method Expression: Calling a method via the Type itself
	f := (*User).GetName
	_ = f(&User{Name: "Bob"})

	// 2. Value Method Expression
	v := User.GetName

	// 3. Pointer Dereferencing and Indexing
	// The parser must resolve the parenthesis before applying the index!
	var a *[3]int
	(*a)[0] = 1

	// 4. Type assertions on function returns directly
	val := GetAny().(string)

	// 5. Instantiating a generic type and invoking a method in one line
	Box[int]{Item: 1}.Print()
}

func GetAny() any { return "test" }