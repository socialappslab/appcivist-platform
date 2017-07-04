# Running and Developing Tests 


## Running Tests

To run tests, open the `activator` console and use the following commants

 - To run all tests, run `test`
 
 - To run only one test class, run `testOnly` followed by the name of the class i.e. `testOnly my.namespace.MyTest`
 
 -To run only the tests that have failed, run `testQuick`
 
 -To run tests continually, run a command with a tilde in front, i.e. `~testQuick`
 
 -To access test helpers such as FakeApplication in console, run `test:console`

-----

For testing we dont want to work with the database if it is possible, it is easier to work with temporary data

The libraries you have to import are:
```
import static play.test.*;
import static play.test.Helpers.*; // to create a fake app with in memory db
import static org.fest.assertions.Assertions.*; // additional asserts than JUnit defaults
```

After that we have to run the fakeapplication with an inmemory database with the following:

```
running(FakeApplication(inMemoryDatabase()),new Runnable(){
	@Override
	public void run(){
	
	}
});
```

The new runnable is the code that we are going to run and test, in this case we are testing a creation method so we first create a new variable 
of the model we are testing, in this case contribution:

```
User creator;
String title="Testing for contribution Model";
String text="Testing the module";
ContributionTypes type=ContributionTypes.COMMENT;
Contribution contribution = new Contribution(creator,title,text,type);
```

After that we save our new contribution

```
contribution.save();
```

Now we retrieve the saved contribution from the database with the following:

```
Contribution savedC=Contribution.find.byId(contribution.id);
```

After that we proceed to check if it is not null:

```
assertThat(savedC).isNotNull;
```

And then we check if the values saved in the database are equal to the ones we have:

```
assertThat(savedC.title).isEqualTo(title);
```










