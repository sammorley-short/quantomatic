(* FIXME: put this in a struct? *)
datatype bbox_op = BBKill of B.name
                 | BBExpand of {
                      bbox : B.name,
                      vmap : VSub.T,
                      bbox_map : BSub.T }
                 | BBCopy of {
                      bbox : B.name,
                      boundary_map : VSub.T,
                      bbox_map : BSub.T }
fun bbox_op_added_bboxes (BBKill _) = B.NSet.empty
  | bbox_op_added_bboxes (BBExpand {bbox_map,...}) = BSub.get_cod_set bbox_map
  | bbox_op_added_bboxes (BBCopy {bbox_map,...}) = BSub.get_cod_set bbox_map
fun bbox_op_get_bbox_copy (BBKill _) _ = raise Match
  | bbox_op_get_bbox_copy (BBExpand {bbox_map,...}) b =
      BSub.get bbox_map b
  | bbox_op_get_bbox_copy (BBCopy {bbox_map,...}) b =
      BSub.get bbox_map b

signature BANG_GRAPH =
sig
  include OGRAPH
  
  exception bbox_not_open_exp of (V.name * V.name) * B.name * T
  exception duplicate_bbox_exp of B.name * T
  exception no_such_bbox_exp of string * B.name * T
  exception bbox_bad_parent_exp of B.name * B.name * T
  exception merge_bbox_exp of string
  
  
  val has_bbox : T -> B.name -> bool
  val get_bboxes : T -> B.NSet.T
  (* Does not contain empty !-boxes (or unboxed vertices) *)
  (*val get_bbox_rel : T -> BVRel.T*)
  (* Return the mapping from parent !-boxes to child !-boxes *)
  (*val get_bbox_child_rel : T -> BBRel.T*)
  
  (* all vertices that are in at least one !-box *)
  val get_bboxed : T -> V.NSet.T
  (* all vertices that are in at no !-boxes *)
  val get_unbboxed : T -> V.NSet.T
  
  (* no_such_vertex_exp if vertex does not exist *)
  val is_bboxed : T -> V.name -> bool
  
  val get_empty_bboxes : T -> B.NSet.T
  
  val add_named_bbox : B.name -> T -> T
  val add_bbox : T -> (B.name * T)
  val add_bbox_anon : T -> T
  
  (* does not delete the contained vertices *)
  (* identity if name does not exist *)
  val delete_bbox : B.name -> T -> T
  
  (* no_such_bbox_exp if !-box does not exist *)
  (* The minimum open subgraph containing the
   * given vertices is added, to ensure the
   * !-box remains an open subgraph.
   *
   * Returns the actual vertices added (always
   * a superset of the vertex set given).
   *
   * TODO: improve name? add_subgraph_to_bbox or something...
   *)
  val add_to_bbox : B.name -> V.NSet.T -> T -> (V.NSet.T * T)
  val add_to_bbox_anon : B.name -> V.NSet.T -> T -> T
  val remove_from_all_bboxes : V.NSet.T -> T -> T
  
  (* no_such_bbox_exp if !-box does not exist *)
  (* bbox_bad_parent_exp if any child !-box contains vertices that will be
   * removed *)
  (* does not raise if verts are not in !-box *)
  (* Does not remove any vertices that are
   * necessary for the !-box to remain open
   *
   * Returns the actual vertices removed (always
   * a subset of the vertex set given).
   *)
  val remove_from_bbox : B.name -> V.NSet.T -> T -> (V.NSet.T * T)
  val remove_from_bbox_anon : B.name -> V.NSet.T -> T -> T
  
  
  (* Set parent of the first bbox to be the second bbox, or clears
   * when the second arg is NONE. *)
  (*val set_bbox_parent_opt : B.name -> B.name option -> T -> T*)
  
  (* raises no_such_bbox_exp if parent or children not in graph *)
  val add_children_to_bbox : B.name -> B.NSet.T -> T -> T
  val add_child_to_bbox : B.name -> B.name -> T -> T
  (* raises no_such_bbox_exp if parent or children not in graph *)
  (* no-op if removing the link would break transitivity *)
  val remove_child_from_bbox : B.name -> B.name -> T -> T
  val clear_bbox_children : B.name -> T -> T
  
  val get_bbox_parents : T -> B.name -> B.NSet.T
  val get_bbox_children : T -> B.name -> B.NSet.T
  
  (* returns true if bbox has parent(s) *)
  val bbox_has_parents : T -> B.name -> bool

  val bbox_has_children : T -> B.name -> bool
  
  (* we don't copy updaters/setters for internal structures,
   * since using these can lead to an inconsistent state *)
  
  (* no_such_bbox_exp if bbox does not exist *)
  val get_vertices_in_bbox : T -> B.name -> V.NSet.T
  
  (* no_such_vertex_exp if vertex does not exist *)
  val get_bboxes_containing_vertex : T -> V.name -> B.NSet.T
  
  (* no_such_bbox_exp *)
  val kill_bbox : B.name -> T -> T
  (* no_such_bbox_exp *)
  val drop_bbox : B.name -> T -> T
  
  val merge_bboxes : B.NSet.T -> T -> B.name * T
  
  val expand_bbox : B.name -> (VSub.T * ESub.T * BSub.T) * T
                           -> (VSub.T * ESub.T * BSub.T) * T
  
  val copy_bbox : B.name -> (VSub.T * ESub.T * BSub.T) * T
                         -> (VSub.T * ESub.T * BSub.T) * T
  
  
  val fresh_copy_bbox : B.name -> T -> (VSub.T * ESub.T * BSub.T) * T
  val fresh_copy_bbox_anon : B.name -> T -> T
  val fresh_expand_bbox : B.name -> T -> (VSub.T * ESub.T * BSub.T) * T
  val fresh_expand_bbox_anon : B.name -> T -> T

  (* apply the given operation and return the new graph and a replayable bbox op *)
  val expand_bbox_op : B.name -> T -> bbox_op * T
  val kill_bbox_op   : B.name -> T -> bbox_op * T
  val copy_bbox_op   : B.name -> T -> bbox_op * T

  val replay_bbox_op  : bbox_op -> T -> T

  (* replay this list of bbox ops, from back to front (as this is how they are stored) *)
  val replay_bbox_ops : bbox_op list -> T -> T
  
  val rename_bang_graph      : (VSub.T * ESub.T * BSub.T) -> T ->
                               (VSub.T * ESub.T * BSub.T) * T
  val rename_bang_graph_anon : (VSub.T * ESub.T * BSub.T) -> T -> T
  val rename_bboxes          : BSub.T -> T -> T
  val rename_bbox            : B.name -> B.name -> T -> T
  val rename_bbox_opt        : B.name -> B.name -> T -> T option


  val get_bbox_annotation : T -> B.name -> Json.jobj
  val get_bbox_annotation_opt : T -> B.name -> Json.jobj option
  val set_bbox_annotation : B.name * Json.jobj -> T -> T
end
