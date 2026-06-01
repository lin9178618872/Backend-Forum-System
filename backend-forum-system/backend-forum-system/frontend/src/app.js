const { useEffect, useMemo, useState } = React;

const h = React.createElement;
const apiBase = "";

async function api(path, options = {}) {
  const response = await fetch(`${apiBase}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = await response.json();
      message = body.detail || body.message || message;
    } catch {
      // Keep the status-based message when the response is not JSON.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

function formatDate(value) {
  if (!value) {
    return "";
  }
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

function App() {
  const [activeTab, setActiveTab] = useState("feed");
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = localStorage.getItem("forum-user");
    return saved ? JSON.parse(saved) : null;
  });
  const [posts, setPosts] = useState([]);
  const [selectedPost, setSelectedPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [hotPostIds, setHotPostIds] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [userForm, setUserForm] = useState({ username: "", email: "" });
  const [postForm, setPostForm] = useState({ title: "", content: "" });
  const [commentText, setCommentText] = useState("");

  const selectedId = selectedPost?.id;

  useEffect(() => {
    loadPosts();
    loadHotPosts();
  }, []);

  useEffect(() => {
    if (selectedId) {
      loadComments(selectedId);
    }
  }, [selectedId]);

  const postCount = posts.length;
  const latestAuthor = posts[0]?.author || "No posts yet";

  async function run(action, success) {
    setLoading(true);
    setMessage("");
    try {
      const result = await action();
      if (success) {
        setMessage(success);
      }
      return result;
    } catch (error) {
      setMessage(error.message);
      return null;
    } finally {
      setLoading(false);
    }
  }

  async function loadPosts() {
    const page = await run(() => api("/api/posts?page=0&size=20"));
    if (page?.content) {
      setPosts(page.content);
    }
  }

  async function loadHotPosts() {
    const ids = await run(() => api("/api/posts/hot"));
    if (Array.isArray(ids)) {
      setHotPostIds(ids);
    }
  }

  async function loadComments(postId) {
    const page = await run(() => api(`/api/posts/${postId}/comments?page=0&size=30`));
    if (page?.content) {
      setComments(page.content);
    }
  }

  async function createUser(event) {
    event.preventDefault();
    const user = await run(
      () => api("/api/users", { method: "POST", body: JSON.stringify(userForm) }),
      "User profile is ready."
    );
    if (user) {
      setCurrentUser(user);
      localStorage.setItem("forum-user", JSON.stringify(user));
      setUserForm({ username: "", email: "" });
    }
  }

  async function createPost(event) {
    event.preventDefault();
    if (!currentUser) {
      setMessage("Create a user first.");
      return;
    }
    const post = await run(
      () => api("/api/posts", {
        method: "POST",
        body: JSON.stringify({ ...postForm, authorId: currentUser.id })
      }),
      "Post published."
    );
    if (post) {
      setPostForm({ title: "", content: "" });
      setSelectedPost(post);
      setActiveTab("detail");
      await loadPosts();
    }
  }

  async function openPost(postId) {
    const post = await run(() => api(`/api/posts/${postId}`));
    if (post) {
      setSelectedPost(post);
      setActiveTab("detail");
      await loadHotPosts();
    }
  }

  async function createComment(event) {
    event.preventDefault();
    if (!currentUser) {
      setMessage("Create a user first.");
      return;
    }
    if (!selectedPost) {
      setMessage("Open a post first.");
      return;
    }
    const comment = await run(
      () => api(`/api/posts/${selectedPost.id}/comments`, {
        method: "POST",
        body: JSON.stringify({ authorId: currentUser.id, content: commentText })
      }),
      "Comment added."
    );
    if (comment) {
      setCommentText("");
      await loadComments(selectedPost.id);
    }
  }

  async function searchPosts(event) {
    event.preventDefault();
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }
    const page = await run(() => api(`/api/search/posts?keyword=${encodeURIComponent(query)}&page=0&size=20`));
    setSearchResults(page?.content || []);
    setActiveTab("search");
  }

  const navItems = useMemo(() => [
    ["feed", "Feed"],
    ["compose", "Compose"],
    ["detail", "Thread"],
    ["search", "Search"]
  ], []);

  return h("div", { className: "app-shell" },
    h("aside", { className: "sidebar" },
      h("div", { className: "brand" },
        h("span", { className: "brand-mark" }, "F"),
        h("div", null,
          h("strong", null, "Forum Console"),
          h("small", null, "Spring Boot API client")
        )
      ),
      h("nav", { className: "nav-list" },
        navItems.map(([key, label]) =>
          h("button", {
            key,
            className: activeTab === key ? "nav-item active" : "nav-item",
            onClick: () => setActiveTab(key)
          }, label)
        )
      ),
      h("section", { className: "user-panel" },
        h("p", { className: "eyebrow" }, "Active user"),
        currentUser
          ? h("div", { className: "current-user" },
              h("strong", null, currentUser.username),
              h("span", null, currentUser.email)
            )
          : h("form", { onSubmit: createUser, className: "stack" },
              h("input", {
                placeholder: "Username",
                value: userForm.username,
                maxLength: 40,
                onChange: event => setUserForm({ ...userForm, username: event.target.value }),
                required: true
              }),
              h("input", {
                placeholder: "Email",
                type: "email",
                value: userForm.email,
                maxLength: 80,
                onChange: event => setUserForm({ ...userForm, email: event.target.value }),
                required: true
              }),
              h("button", { className: "primary", disabled: loading }, "Create User")
            )
      )
    ),
    h("main", { className: "main" },
      h("header", { className: "topbar" },
        h("div", null,
          h("p", { className: "eyebrow" }, "Backend Forum System"),
          h("h1", null, "Posts, comments, cache, and search")
        ),
        h("form", { className: "search-box", onSubmit: searchPosts },
          h("input", {
            placeholder: "Search posts",
            value: query,
            onChange: event => setQuery(event.target.value)
          }),
          h("button", { disabled: loading }, "Search")
        )
      ),
      h("section", { className: "metrics" },
        h(Metric, { label: "Posts loaded", value: postCount }),
        h(Metric, { label: "Latest author", value: latestAuthor }),
        h(Metric, { label: "Hot IDs", value: hotPostIds.length || "0" })
      ),
      message && h("div", { className: message.includes("ready") || message.includes("published") || message.includes("added") ? "notice ok" : "notice" }, message),
      activeTab === "feed" && h(FeedView, { posts, openPost, refresh: loadPosts, loading }),
      activeTab === "compose" && h(ComposeView, { postForm, setPostForm, createPost, loading, currentUser }),
      activeTab === "detail" && h(DetailView, { selectedPost, comments, commentText, setCommentText, createComment, openFeed: () => setActiveTab("feed"), loading }),
      activeTab === "search" && h(SearchView, { query, searchResults, openPost })
    )
  );
}

function Metric({ label, value }) {
  return h("div", { className: "metric" },
    h("span", null, label),
    h("strong", null, value)
  );
}

function FeedView({ posts, openPost, refresh, loading }) {
  return h("section", { className: "content-grid" },
    h("div", { className: "section-head" },
      h("h2", null, "Discussion Feed"),
      h("button", { onClick: refresh, disabled: loading }, "Refresh")
    ),
    posts.length === 0
      ? h("div", { className: "empty" }, "No posts yet. Create the first thread.")
      : h("div", { className: "post-list" },
          posts.map(post => h(PostRow, { key: post.id, post, openPost }))
        )
  );
}

function PostRow({ post, openPost }) {
  return h("article", { className: "post-row", onClick: () => openPost(post.id), tabIndex: 0 },
    h("div", null,
      h("h3", null, post.title),
      h("p", null, post.content),
      h("div", { className: "meta" },
        h("span", null, post.author),
        h("span", null, formatDate(post.createdAt))
      )
    ),
    h("strong", { className: "view-count" }, `${post.viewCount} views`)
  );
}

function ComposeView({ postForm, setPostForm, createPost, loading, currentUser }) {
  return h("section", { className: "editor" },
    h("div", { className: "section-head" },
      h("h2", null, "Create Thread"),
      h("span", { className: "status-pill" }, currentUser ? `Posting as ${currentUser.username}` : "Create a user first")
    ),
    h("form", { onSubmit: createPost, className: "compose-form" },
      h("input", {
        placeholder: "Thread title",
        value: postForm.title,
        maxLength: 120,
        onChange: event => setPostForm({ ...postForm, title: event.target.value }),
        required: true
      }),
      h("textarea", {
        placeholder: "Write the post content",
        value: postForm.content,
        maxLength: 10000,
        onChange: event => setPostForm({ ...postForm, content: event.target.value }),
        required: true
      }),
      h("button", { className: "primary wide", disabled: loading || !currentUser }, "Publish")
    )
  );
}

function DetailView({ selectedPost, comments, commentText, setCommentText, createComment, openFeed, loading }) {
  if (!selectedPost) {
    return h("section", { className: "empty" },
      h("p", null, "Open a post from the feed to view the thread."),
      h("button", { onClick: openFeed }, "Go to Feed")
    );
  }

  return h("section", { className: "thread" },
    h("article", { className: "thread-post" },
      h("div", { className: "meta" },
        h("span", null, selectedPost.author),
        h("span", null, formatDate(selectedPost.createdAt)),
        h("span", null, `${selectedPost.viewCount} views`)
      ),
      h("h2", null, selectedPost.title),
      h("p", null, selectedPost.content)
    ),
    h("div", { className: "section-head compact" },
      h("h2", null, "Comments"),
      h("span", { className: "status-pill" }, comments.length)
    ),
    h("form", { className: "comment-form", onSubmit: createComment },
      h("input", {
        placeholder: "Add a comment",
        value: commentText,
        maxLength: 1000,
        onChange: event => setCommentText(event.target.value),
        required: true
      }),
      h("button", { disabled: loading }, "Reply")
    ),
    h("div", { className: "comment-list" },
      comments.length === 0
        ? h("div", { className: "empty slim" }, "No comments yet.")
        : comments.map(comment => h("article", { className: "comment", key: comment.id },
            h("strong", null, comment.author),
            h("p", null, comment.content),
            h("span", null, formatDate(comment.createdAt))
          ))
    )
  );
}

function SearchView({ query, searchResults, openPost }) {
  return h("section", { className: "content-grid" },
    h("div", { className: "section-head" },
      h("h2", null, "Search Results"),
      h("span", { className: "status-pill" }, query || "No keyword")
    ),
    searchResults.length === 0
      ? h("div", { className: "empty" }, "Run a search from the top bar. Elasticsearch must be running for live search.")
      : h("div", { className: "post-list" },
          searchResults.map(post => h(PostRow, { key: post.id, post, openPost }))
        )
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(h(App));
