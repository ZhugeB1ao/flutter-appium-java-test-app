class Task {
  final int id;
  String title;
  String? description;
  bool done;

  Task({required this.id, required this.title, this.description, this.done = false});

  factory Task.fromJson(Map<String, dynamic> json) => Task(
        id: json['id'] as int,
        title: json['title'] as String,
        description: json['description'] as String?,
        done: json['done'] as bool? ?? false,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'description': description,
        'done': done,
      };
}
