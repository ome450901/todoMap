package com.todomap.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todomap.database.Todo
import com.todomap.databinding.ListItemTodoBinding

/**
 * @author WeiYi Yu
 * @date 2020-04-29
 */
class TodoAdapter : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = getItem(position)
        holder.bind(todo)
    }

    class TodoViewHolder private constructor(private val binding: ListItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: Todo) {
            binding.tvTitle.setText(todo.title)
        }

        companion object {
            fun from(parent: ViewGroup): TodoViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListItemTodoBinding.inflate(inflater, parent, false)
                return TodoViewHolder(binding)
            }
        }
    }

    private class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.todoId == newItem.todoId
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }
}