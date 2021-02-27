# bindin

[![](https://jitpack.io/v/AChep/bindin.svg)](https://jitpack.io/#AChep/bindin)

Utility library for Android with Kotlin to help you to replace the `LiveData` with the `Flow` on the presentation layer.

Download
----------------
Gradle:
```groovy
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'com.github.AChep:bindin:${latestVersion}'
}
```

Usage
----------------
Let's say you have an existing setup with the `LiveData`:
```kotlin
val liveData = MutableLiveData<Boolean>()
liveData.observe(viewLifecycleOwner) {
  println(it)
}
```

to migrate it you would have to use the `bindIn` function:
```kotlin
val liveData = MutableLiveData<Boolean>()
viewLifecycleOwner.bindIn(liveData) {
  println(it)
}
```

Huh. That looks almost identical, right? The _real benefit_ is that it works for `Flow`s with the exact same syntax!
```kotlin
val flow = MutableStateFlow<Boolean>(false)
viewLifecycleOwner.bindIn(flow) {
  println(it)
}
```

It's guaranteed that the block will only be called on the Main thread and the lifecycle will be in required state!
You can specify the minimum state using the `minimumLifecycleState` argument.

Advanced usage
----------------
If you want to execute a block instead of subscribing to a flow, use the `bindBlock` function.
```kotlin
viewLifecycleOwner.bindBlock {
  println(it)
}
```

Invoking the `bindIn` function returns the `InBinding` object. To unbind, invoke its `unbind` lambda.
```kotlin
val flow = MutableStateFlow<Boolean>(false)
val binding = viewLifecycleOwner.bindIn(flow) {
  println(it)
}
// ...
binding.unbind()
```

To implement two way binding you may use the `bindOut` method of the `InBinding` class. An example
usage:
```kotlin
val flow = MutableStateFlow<Boolean>(false)
val binding = viewLifecycleOwner.bindIn(flow) {
  button.isChecked = it
}.bindOut(
  observe = { observer ->
    button.setOnCheckedChangeListener { _, isChecked ->
      observer(isChecked)
    }
  },
  pipe = {
    flow.value = it
  },
)
```
for some of the views there's already a `bindOut` function
```kotlin
val flow = MutableStateFlow<Boolean>(false)
val binding = viewLifecycleOwner.bindIn(flow) {
  button.isChecked = it
}.bindOut(button) {
  flow.value = it
}
```

Report a bug or request a feature
----------------
Before creating a new issue please make sure that same or similar issue is not already created by checking [open issues][2] and [closed issues][3] *(please note that there might be multiple pages)*. If your issue is already there, don't create a new one, but leave a comment under already existing one.

Checklist for creating issues:

- Keep titles short but descriptive.
- For feature requests leave a clear description about the feature with examples where appropriate.
- For bug reports leave as much information as possible about your device, android version, etc.
- For bug reports also write steps to reproduce the issue.

[Create new issue][1]

Versioning
----------------
For transparency in a release cycle and in striving to maintain backward compatibility, a project should be maintained under the Semantic Versioning guidelines. Sometimes we screw up, but we should adhere to these rules whenever possible.

Releases will be numbered with the following format: `<major>.<minor>.<patch>` and constructed with the following guidelines:
- Breaking backward compatibility bumps the major while resetting minor and patch
- New additions without breaking backward compatibility bumps the minor while resetting the patch
- Bug fixes and misc changes bumps only the patch

For more information on SemVer, please visit http://semver.org/.

Build
----------------
Clone the project and come in:

``` bash
$ git clone git://github.com/AChep/bindin.git
$ cd bindin/
```

[1]: https://github.com/AChep/bindin/issues/new
[2]: https://github.com/AChep/bindin/issues?state=open
[3]: https://github.com/AChep/bindin/issues?state=closed
[4]: https://github.com/AChep/bindin/tree/master/sample